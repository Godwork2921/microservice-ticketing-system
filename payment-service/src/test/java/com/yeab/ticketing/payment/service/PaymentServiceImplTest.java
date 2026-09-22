package com.yeab.ticketing.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeab.ticketing.payment.client.ReservationClient;
import com.yeab.ticketing.payment.client.ReservationDetails;
import com.yeab.ticketing.payment.dto.CreatePaymentRequest;
import com.yeab.ticketing.payment.dto.FailPaymentRequest;
import com.yeab.ticketing.payment.dto.InvoiceResponse;
import com.yeab.ticketing.payment.dto.ReceiptResponse;
import com.yeab.ticketing.payment.entity.PaymentEntity;
import com.yeab.ticketing.payment.enums.InvoiceStatus;
import com.yeab.ticketing.payment.enums.PaymentStatus;
import com.yeab.ticketing.payment.exception.InvalidPaymentStateException;
import com.yeab.ticketing.payment.exception.WebhookVerificationException;
import com.yeab.ticketing.payment.messaging.PaymentEventOutbox;
import com.yeab.ticketing.payment.provider.MockPaymentProvider;
import com.yeab.ticketing.payment.provider.PaymentProviderFactory;
import com.yeab.ticketing.payment.repository.OutboxRepository;
import com.yeab.ticketing.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {
    @Mock
    private PaymentRepository repository;
    @Mock
    private OutboxRepository outboxRepository;
    @Mock
    private ReservationClient reservationClient;
    @Mock
    private InvoiceReceiptService invoiceReceiptService;

    private PaymentServiceImpl service;
    private ObjectMapper mapper;
    private String webhookSecret;
    private UUID reservationId;
    private UUID paymentId;
    private Clock clock;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper().findAndRegisterModules();
        webhookSecret = "unit-test-secret";
        paymentId = UUID.randomUUID();
        reservationId = UUID.randomUUID();
        clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

        MockPaymentProvider mockProvider = new MockPaymentProvider();
        PaymentProviderFactory factory = new PaymentProviderFactory(Map.of("mock", mockProvider), "mock");
        service = new PaymentServiceImpl(repository, reservationClient, factory,
                new PaymentEventOutbox(outboxRepository, mapper), invoiceReceiptService, mapper, webhookSecret, clock);
    }

    @Test
    void createUsesServerSideAmountAndCurrency() {
        when(reservationClient.getReservation(reservationId))
                .thenReturn(new ReservationDetails(reservationId, "HOLD", new BigDecimal("145.0000"),
                        "USD", "customer-1", "customer-1@example.com"));
        when(repository.saveAndFlush(any(PaymentEntity.class))).thenAnswer(invocation -> {
            PaymentEntity entity = invocation.getArgument(0);
            entity.setId(paymentId);
            return entity;
        });
        when(repository.save(any(PaymentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(invoiceReceiptService.createInvoice(any(PaymentEntity.class)))
                .thenReturn(stubInvoice());

        var response = service.create(new CreatePaymentRequest(reservationId, "whatever",
                new BigDecimal("145.0000"), "usd", "mock", null, null, null, null, null, null));

        assertThat(response.id()).isEqualTo(paymentId);
        assertThat(response.amount()).isEqualByComparingTo("145.0000");
        assertThat(response.currency()).isEqualTo("USD");
        assertThat(response.customerId()).isEqualTo("customer-1");
        assertThat(response.provider()).isEqualTo("mock");
        assertThat(response.status()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void createRejectsWhenReservationIsNotHold() {
        when(reservationClient.getReservation(reservationId))
                .thenReturn(new ReservationDetails(reservationId, "CANCELLED", new BigDecimal("145.0000"),
                        "USD", "customer-1", "customer-1@example.com"));

        assertThatThrownBy(() -> service.create(request(new BigDecimal("145.0000"))))
                .isInstanceOf(InvalidPaymentStateException.class);
    }

    @Test
    void createRejectsAmountMismatch() {
        when(reservationClient.getReservation(reservationId))
                .thenReturn(new ReservationDetails(reservationId, "HOLD", new BigDecimal("145.0000"),
                        "USD", "customer-1", "customer-1@example.com"));

        assertThatThrownBy(() -> service.create(request(new BigDecimal("10.00"))))
                .isInstanceOf(InvalidPaymentStateException.class);
    }

    @Test
    void succeedIsIdempotentAfterFirstTransition() {
        PaymentEntity pending = payment(PaymentStatus.PENDING);
        PaymentEntity succeeded = payment(PaymentStatus.SUCCEEDED);
        succeeded.setProviderPaymentId("ref-1");
        when(repository.findById(paymentId)).thenReturn(Optional.of(pending), Optional.of(succeeded));
        when(repository.transitionToSucceeded(paymentId, "ref-1", clock.instant())).thenReturn(1);
        when(invoiceReceiptService.createReceipt(any(PaymentEntity.class)))
                .thenReturn(stubReceipt());

        var first = service.succeed(paymentId, "ref-1");

        assertThat(first.status()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(first.providerPaymentId()).isEqualTo("ref-1");
    }

    @Test
    void webhookWithBadSignatureIsRejected() {
        assertThatThrownBy(() -> service.handleWebhook("mock",
                "{\"txRef\":\"x\"}", "deadbeef"))
                .isInstanceOf(WebhookVerificationException.class);
    }

    @Test
    void signedSuccessWebhookSucceedsPayment() throws Exception {
        String txRef = "tx-" + paymentId;
        PaymentEntity pending = payment(PaymentStatus.PENDING);
        pending.setProviderPaymentId(txRef);
        PaymentEntity succeeded = payment(PaymentStatus.SUCCEEDED);
        succeeded.setProviderPaymentId(txRef);
        when(repository.findByProviderPaymentId(txRef)).thenReturn(Optional.of(pending));
        when(repository.findById(paymentId)).thenReturn(Optional.of(pending), Optional.of(succeeded));
        when(repository.transitionToSucceeded(paymentId, "provider-secondary-ref", clock.instant())).thenReturn(1);
        when(invoiceReceiptService.createReceipt(any(PaymentEntity.class)))
                .thenReturn(stubReceipt());

        String body = mapper.writeValueAsString(Map.of(
                "txRef", txRef, "status", "SUCCESS",
                "amount", "145.0000", "currency", "USD", "reference", "provider-secondary-ref"));

        var response = service.handleWebhook("mock", body, sign(body));

        assertThat(response.status()).isEqualTo(PaymentStatus.SUCCEEDED);
    }

    private CreatePaymentRequest request(BigDecimal amount) {
        return new CreatePaymentRequest(reservationId, "customer-1", amount, "USD",
                "mock", null, null, null, null, null, null);
    }

    private PaymentEntity payment(PaymentStatus status) {
        PaymentEntity entity = new PaymentEntity(reservationId, "customer-1",
                new BigDecimal("145.0000"), "USD", "mock");
        entity.setId(paymentId);
        entity.setStatus(status);
        return entity;
    }

    private String sign(String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
    }

    private InvoiceResponse stubInvoice() {
        return new InvoiceResponse(UUID.randomUUID(), paymentId, reservationId,
                "customer-1", "INV-20260101-XXXXXXXX", new BigDecimal("145.0000"),
                "USD", InvoiceStatus.ISSUED, clock.instant(), clock.instant().plusSeconds(3600),
                null, clock.instant());
    }

    private ReceiptResponse stubReceipt() {
        return new ReceiptResponse(UUID.randomUUID(), paymentId, null, reservationId,
                "customer-1", "REC-20260101-XXXXXXXX", new BigDecimal("145.0000"),
                "USD", "mock", "ref-1", clock.instant(), null, clock.instant());
    }
}