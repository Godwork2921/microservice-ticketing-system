package com.yeab.ticketing.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents a receipt issued when a payment succeeds.
 * Immutable proof of payment — never updated after creation.
 */
@Entity
@Table(name = "receipts")
public class ReceiptEntity {

    @Id
    private UUID id;

    @Column(name = "payment_id", nullable = false, unique = true)
    private UUID paymentId;

    @Column(name = "invoice_id")
    private UUID invoiceId;

    @Column(name = "reservation_id", nullable = false)
    private UUID reservationId;

    @Column(name = "customer_id", nullable = false, length = 200)
    private String customerId;

    @Column(name = "receipt_number", nullable = false, unique = true, length = 50)
    private String receiptNumber;

    @Column(name = "amount_paid", nullable = false, precision = 19, scale = 4)
    private BigDecimal amountPaid;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 50)
    private String provider;

    @Column(name = "provider_payment_id", length = 200)
    private String providerPaymentId;

    @Column(name = "paid_at", nullable = false)
    private Instant paidAt;

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ReceiptEntity() { }

    public ReceiptEntity(UUID paymentId, UUID invoiceId, UUID reservationId, String customerId,
                         String receiptNumber, BigDecimal amountPaid, String currency,
                         String provider, String providerPaymentId, Instant paidAt) {
        this.paymentId = paymentId;
        this.invoiceId = invoiceId;
        this.reservationId = reservationId;
        this.customerId = customerId;
        this.receiptNumber = receiptNumber;
        this.amountPaid = amountPaid;
        this.currency = currency;
        this.provider = provider;
        this.providerPaymentId = providerPaymentId;
        this.paidAt = paidAt;
    }

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getPaymentId() { return paymentId; }
    public UUID getInvoiceId() { return invoiceId; }
    public UUID getReservationId() { return reservationId; }
    public String getCustomerId() { return customerId; }
    public String getReceiptNumber() { return receiptNumber; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public String getCurrency() { return currency; }
    public String getProvider() { return provider; }
    public String getProviderPaymentId() { return providerPaymentId; }
    public Instant getPaidAt() { return paidAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Instant getCreatedAt() { return createdAt; }
}
