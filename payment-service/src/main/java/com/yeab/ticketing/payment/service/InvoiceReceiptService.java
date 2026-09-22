package com.yeab.ticketing.payment.service;

import com.yeab.ticketing.payment.dto.InvoiceResponse;
import com.yeab.ticketing.payment.dto.ReceiptResponse;
import com.yeab.ticketing.payment.entity.InvoiceEntity;
import com.yeab.ticketing.payment.entity.PaymentEntity;
import com.yeab.ticketing.payment.entity.ReceiptEntity;
import com.yeab.ticketing.payment.enums.InvoiceStatus;
import com.yeab.ticketing.payment.exception.PaymentNotFoundException;
import com.yeab.ticketing.payment.mapper.InvoiceMapper;
import com.yeab.ticketing.payment.mapper.ReceiptMapper;
import com.yeab.ticketing.payment.repository.InvoiceRepository;
import com.yeab.ticketing.payment.repository.PaymentRepository;
import com.yeab.ticketing.payment.repository.ReceiptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Manages invoice and receipt lifecycle.
 *
 * Invoice lifecycle:
 *   ISSUED (on payment create) → PAID (on payment succeed) or VOID (on payment fail)
 *
 * Receipt lifecycle:
 *   Created once when payment succeeds. Immutable after creation.
 */
@Service
public class InvoiceReceiptService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceReceiptService.class);
    private static final DateTimeFormatter NUMBER_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);

    private final InvoiceRepository invoiceRepository;
    private final ReceiptRepository receiptRepository;
    private final PaymentRepository paymentRepository;
    private final Clock clock;

    public InvoiceReceiptService(InvoiceRepository invoiceRepository,
                                 ReceiptRepository receiptRepository,
                                 PaymentRepository paymentRepository,
                                 Clock clock) {
        this.invoiceRepository = invoiceRepository;
        this.receiptRepository = receiptRepository;
        this.paymentRepository = paymentRepository;
        this.clock = clock;
    }

    /**
     * Creates an invoice for a newly initiated payment.
     * Idempotent — returns existing invoice if one already exists for this payment.
     */
    @Transactional
    public InvoiceResponse createInvoice(PaymentEntity payment) {
        return invoiceRepository.findByPaymentId(payment.getId())
                .map(InvoiceMapper::toResponse)
                .orElseGet(() -> {
                    Instant now = clock.instant();
                    String invoiceNumber = generateInvoiceNumber(now);
                    // Due immediately — payment is expected before checkout completes
                    InvoiceEntity invoice = new InvoiceEntity(
                            payment.getId(),
                            payment.getReservationId(),
                            payment.getCustomerId(),
                            invoiceNumber,
                            payment.getAmount(),
                            payment.getCurrency(),
                            now,
                            now.plusSeconds(3600) // 1 hour due window
                    );
                    InvoiceEntity saved = invoiceRepository.save(invoice);
                    log.info("Created invoice {} for payment {}", invoiceNumber, payment.getId());
                    return InvoiceMapper.toResponse(saved);
                });
    }

    /**
     * Marks invoice as PAID and creates a receipt.
     * Called when payment transitions to SUCCEEDED.
     * Idempotent — safe to call multiple times.
     */
    @Transactional
    public ReceiptResponse createReceipt(PaymentEntity payment) {
        // Return existing receipt if already created (idempotent)
        return receiptRepository.findByPaymentId(payment.getId())
                .map(ReceiptMapper::toResponse)
                .orElseGet(() -> {
                    Instant paidAt = payment.getCompletedAt() != null
                            ? payment.getCompletedAt() : clock.instant();

                    // Mark invoice as PAID
                    invoiceRepository.findByPaymentId(payment.getId()).ifPresent(invoice -> {
                        invoice.setStatus(InvoiceStatus.PAID);
                        invoiceRepository.save(invoice);
                        log.info("Marked invoice {} as PAID", invoice.getInvoiceNumber());
                    });

                    // Find the invoice id to link on the receipt
                    UUID invoiceId = invoiceRepository.findByPaymentId(payment.getId())
                            .map(InvoiceEntity::getId).orElse(null);

                    String receiptNumber = generateReceiptNumber(paidAt);
                    ReceiptEntity receipt = new ReceiptEntity(
                            payment.getId(),
                            invoiceId,
                            payment.getReservationId(),
                            payment.getCustomerId(),
                            receiptNumber,
                            payment.getAmount(),
                            payment.getCurrency(),
                            payment.getProvider(),
                            payment.getProviderPaymentId(),
                            paidAt
                    );
                    ReceiptEntity saved = receiptRepository.save(receipt);
                    log.info("Created receipt {} for payment {}", receiptNumber, payment.getId());
                    return ReceiptMapper.toResponse(saved);
                });
    }

    /**
     * Marks invoice as VOID when payment fails.
     */
    @Transactional
    public void voidInvoice(UUID paymentId) {
        invoiceRepository.findByPaymentId(paymentId).ifPresent(invoice -> {
            if (invoice.getStatus() == InvoiceStatus.ISSUED) {
                invoice.setStatus(InvoiceStatus.VOID);
                invoiceRepository.save(invoice);
                log.info("Voided invoice {} for failed payment {}", invoice.getInvoiceNumber(), paymentId);
            }
        });
    }

    // --- Query methods ---

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(UUID id) {
        return invoiceRepository.findById(id)
                .map(InvoiceMapper::toResponse)
                .orElseThrow(() -> new PaymentNotFoundException("Invoice " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByPaymentId(UUID paymentId) {
        return invoiceRepository.findByPaymentId(paymentId)
                .map(InvoiceMapper::toResponse)
                .orElseThrow(() -> new PaymentNotFoundException("No invoice for payment " + paymentId));
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByReservationId(UUID reservationId) {
        return invoiceRepository.findByReservationId(reservationId)
                .map(InvoiceMapper::toResponse)
                .orElseThrow(() -> new PaymentNotFoundException("No invoice for reservation " + reservationId));
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> listInvoices(String customerId, Pageable pageable) {
        return invoiceRepository.findByCustomerIdOrderByIssuedAtDesc(customerId, pageable)
                .map(InvoiceMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ReceiptResponse getReceiptById(UUID id) {
        return receiptRepository.findById(id)
                .map(ReceiptMapper::toResponse)
                .orElseThrow(() -> new PaymentNotFoundException("Receipt " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public ReceiptResponse getReceiptByPaymentId(UUID paymentId) {
        return receiptRepository.findByPaymentId(paymentId)
                .map(ReceiptMapper::toResponse)
                .orElseThrow(() -> new PaymentNotFoundException("No receipt for payment " + paymentId));
    }

    @Transactional(readOnly = true)
    public ReceiptResponse getReceiptByReservationId(UUID reservationId) {
        return receiptRepository.findByReservationId(reservationId)
                .map(ReceiptMapper::toResponse)
                .orElseThrow(() -> new PaymentNotFoundException("No receipt for reservation " + reservationId));
    }

    @Transactional(readOnly = true)
    public Page<ReceiptResponse> listReceipts(String customerId, Pageable pageable) {
        return receiptRepository.findByCustomerIdOrderByPaidAtDesc(customerId, pageable)
                .map(ReceiptMapper::toResponse);
    }

    // --- Number generation ---

    private String generateInvoiceNumber(Instant issuedAt) {
        String date = NUMBER_FORMAT.format(issuedAt);
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "INV-" + date + "-" + suffix;
    }

    private String generateReceiptNumber(Instant paidAt) {
        String date = NUMBER_FORMAT.format(paidAt);
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "REC-" + date + "-" + suffix;
    }
}
