package com.yeab.ticketing.payment.service;

import com.yeab.ticketing.payment.client.ReservationClient;
import com.yeab.ticketing.payment.client.ReservationDetails;
import com.yeab.ticketing.payment.dto.CreatePaymentRequest;
import com.yeab.ticketing.payment.dto.FailPaymentRequest;
import com.yeab.ticketing.payment.dto.PaymentResponse;
import com.yeab.ticketing.payment.dto.WebhookPayload;
import com.yeab.ticketing.payment.entity.PaymentEntity;
import com.yeab.ticketing.payment.enums.PaymentStatus;
import com.yeab.ticketing.payment.exception.ChapaIntegrationException;
import com.yeab.ticketing.payment.exception.InvalidPaymentStateException;
import com.yeab.ticketing.payment.exception.PaymentNotFoundException;
import com.yeab.ticketing.payment.exception.WebhookVerificationException;
import com.yeab.ticketing.payment.mapper.PaymentMapper;
import com.yeab.ticketing.payment.messaging.PaymentEventOutbox;
import com.yeab.ticketing.payment.provider.PaymentProvider;
import com.yeab.ticketing.payment.provider.PaymentProviderFactory;
import com.yeab.ticketing.payment.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository repository;
    private final ReservationClient reservationClient;
    private final PaymentProviderFactory providerFactory;
    private final PaymentEventOutbox eventOutbox;
    private final InvoiceReceiptService invoiceReceiptService;
    private final ObjectMapper objectMapper;
    private final String webhookSecret;
    private final Clock clock;

    public PaymentServiceImpl(PaymentRepository repository,
                              ReservationClient reservationClient,
                              PaymentProviderFactory providerFactory,
                              PaymentEventOutbox eventOutbox,
                              InvoiceReceiptService invoiceReceiptService,
                              ObjectMapper objectMapper,
                              @Value("${app.payment.webhook-secret:}") String webhookSecret,
                              Clock clock) {
        this.repository = repository;
        this.reservationClient = reservationClient;
        this.providerFactory = providerFactory;
        this.eventOutbox = eventOutbox;
        this.invoiceReceiptService = invoiceReceiptService;
        this.objectMapper = objectMapper;
        this.webhookSecret = webhookSecret;
        this.clock = clock;
    }

    @Override
    @Transactional
    public PaymentResponse create(CreatePaymentRequest request) {
        PaymentEntity existing = repository.findByReservationId(request.reservationId()).orElse(null);
        if (existing != null) {
            log.info("Payment already exists for reservation {}, returning {} ({})",
                    request.reservationId(), existing.getId(), existing.getStatus());
            return PaymentMapper.toResponse(existing);
        }

        // The amount, currency and customer come from reservation-service, never the client.
        ReservationDetails reservation = reservationClient.getReservation(request.reservationId());
        if (reservation.totalAmount() == null) {
            throw new InvalidPaymentStateException("Reservation has no payable amount");
        }
        if (!"HOLD".equals(reservation.status())) {
            throw new InvalidPaymentStateException(
                    "Reservation " + request.reservationId() + " is not in a payable state ("
                            + reservation.status() + ")");
        }
        if (request.amount().compareTo(reservation.totalAmount()) != 0) {
            throw new InvalidPaymentStateException(
                    "Amount mismatch: server total is " + reservation.totalAmount()
                            + " but client sent " + request.amount());
        }
        String currency = reservation.currency().toUpperCase();
        if (!currency.equals(request.currency().toUpperCase())) {
            throw new InvalidPaymentStateException("Currency mismatch with the reservation");
        }

        PaymentProvider provider = providerFactory.get();
        if (!provider.name().equalsIgnoreCase(request.provider())) {
            throw new ChapaIntegrationException(
                    "Configured provider is '" + provider.name() + "', not '" + request.provider() + "'");
        }

        PaymentEntity payment = new PaymentEntity(
                request.reservationId(), reservation.customerId(), reservation.totalAmount(),
                currency, provider.name());
        payment = repository.saveAndFlush(payment);

        String txRef = "tx-" + payment.getId();
        PaymentProvider.Reference reference = provider.initialize(new PaymentProvider.Initiation(
                reservation.totalAmount(), currency, request.email(),
                request.firstName(), request.lastName(), request.phoneNumber(),
                txRef, request.callbackUrl(), request.returnUrl()));
        payment.setProviderPaymentId(reference.providerId());
        payment.setCheckoutUrl(reference.checkoutUrl());
        payment = repository.save(payment);

        eventOutbox.recordInitiated(payment.getId(), payment.getReservationId(), payment.getCustomerId(),
                payment.getCurrency(), payment.getAmount(), payment.getProvider());

        // Create invoice for this payment
        invoiceReceiptService.createInvoice(payment);

        return PaymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getById(UUID paymentId) {
        return PaymentMapper.toResponse(find(paymentId));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getByReservationId(UUID reservationId) {
        return repository.findByReservationId(reservationId)
                .map(PaymentMapper::toResponse)
                .orElseThrow(() -> new PaymentNotFoundException(reservationId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> list(String customerId, Pageable pageable) {
        Page<PaymentEntity> page = customerId == null || customerId.isBlank()
                ? repository.findAll(pageable)
                : repository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable);
        return page.map(PaymentMapper::toResponse);
    }

    @Override
    @Transactional
    public PaymentResponse succeed(UUID paymentId, String providerPaymentId) {
        PaymentEntity payment = find(paymentId);
        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            return PaymentMapper.toResponse(payment);
        }
        String resolvedRef = providerPaymentId == null || providerPaymentId.isBlank()
                ? "ref-" + payment.getId() : providerPaymentId.trim();
        int updated = repository.transitionToSucceeded(paymentId, resolvedRef, clock.instant());
        if (updated != 1) {
            throw new InvalidPaymentStateException(
                    "Payment " + paymentId + " cannot transition from " + payment.getStatus());
        }
        PaymentEntity succeeded = find(paymentId);
        eventOutbox.recordSuccessful(succeeded.getId(), succeeded.getReservationId(), succeeded.getCustomerId(),
                succeeded.getCurrency(), succeeded.getAmount(), succeeded.getProvider(), resolvedRef);

        // Create receipt and mark invoice as PAID
        invoiceReceiptService.createReceipt(succeeded);

        return PaymentMapper.toResponse(succeeded);
    }

    @Override
    @Transactional
    public PaymentResponse fail(UUID paymentId, FailPaymentRequest request) {
        PaymentEntity payment = find(paymentId);
        if (payment.getStatus() == PaymentStatus.FAILED) {
            return PaymentMapper.toResponse(payment);
        }
        String reason = request.reason().trim();
        int updated = repository.transitionToFailed(paymentId, reason, clock.instant());
        if (updated != 1) {
            throw new InvalidPaymentStateException(
                    "Payment " + paymentId + " cannot transition from " + payment.getStatus());
        }
        PaymentEntity failed = find(paymentId);
        eventOutbox.recordFailed(failed.getId(), failed.getReservationId(), failed.getCustomerId(),
                failed.getCurrency(), failed.getAmount(), failed.getProvider(), reason);

        // Void the invoice for this failed payment
        invoiceReceiptService.voidInvoice(paymentId);

        return PaymentMapper.toResponse(failed);
    }

    @Override
    @Transactional
    public PaymentResponse verify(String providerId) {
        PaymentEntity payment = repository.findByProviderPaymentId(providerId)
                .orElseThrow(() -> new PaymentNotFoundException(providerId));
        PaymentProvider.Verification verification = providerFactory.get().verify(providerId);
        if (!verification.successful() || !verification.amountMatches()
                || verification.amount() == null
                || payment.getAmount().compareTo(verification.amount()) != 0
                || !payment.getCurrency().equalsIgnoreCase(verification.currency())) {
            throw new InvalidPaymentStateException(
                    "Verification did not match payment " + payment.getId() + ": " + verification.message());
        }
        String reference = verification.reference() != null ? verification.reference() : providerId;
        return succeed(payment.getId(), reference);
    }

    @Override
    @Transactional
    public PaymentResponse handleWebhook(String provider, String rawBody, String signature) {
        verifySignature(rawBody, signature);
        WebhookPayload payload = parseBody(rawBody);
        if (!providerFactory.get().name().equalsIgnoreCase(provider)) {
            throw new WebhookVerificationException(
                    "Webhook for unknown provider '" + provider + "' (active: " + providerFactory.activeName() + ")");
        }
        if (payload.txRef() == null || payload.txRef().isBlank()) {
            throw new WebhookVerificationException("Webhook payload is missing txRef");
        }
        PaymentEntity payment = repository.findByProviderPaymentId(payload.txRef().trim())
                .orElseThrow(() -> new PaymentNotFoundException(payload.txRef().trim()));

        PaymentProvider.Verification verification = providerFactory.get()
                .applyWebhook(payment.getProviderPaymentId(), payload.status(), payload.amount(),
                        payload.currency(), payload.reference());
        if (!verification.successful()) {
            String reason = verification.message() != null ? verification.message() : "Payment declined by provider";
            return fail(payment.getId(), new FailPaymentRequest(reason));
        }
        if (verification.amount() != null && payment.getAmount().compareTo(verification.amount()) != 0) {
            throw new WebhookVerificationException(
                    "Webhook amount " + verification.amount() + " does not match payment " + payment.getId());
        }
        String reference = verification.reference() != null ? verification.reference() : payload.reference();
        return succeed(payment.getId(), reference);
    }

    private WebhookPayload parseBody(String rawBody) {
        try {
            return objectMapper.readValue(rawBody, WebhookPayload.class);
        } catch (Exception ex) {
            throw new WebhookVerificationException("Webhook body is not valid JSON");
        }
    }

    private void verifySignature(String rawBody, String signature) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new WebhookVerificationException("APP_PAYMENT_WEBHOOK_SECRET is not configured");
        }
        if (signature == null || signature.isBlank()) {
            throw new WebhookVerificationException("Missing X-Webhook-Signature header");
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String expected = HexFormat.of().formatHex(mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8)));
            if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                    signature.trim().toLowerCase().getBytes(StandardCharsets.UTF_8))) {
                throw new WebhookVerificationException("Webhook signature does not match");
            }
        } catch (WebhookVerificationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new WebhookVerificationException("Could not verify webhook signature");
        }
    }

    private PaymentEntity find(UUID id) {
        return repository.findById(id).orElseThrow(() -> new PaymentNotFoundException(id));
    }
}