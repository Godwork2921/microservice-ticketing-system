package com.yeab.ticketing.payment.entity;

import com.yeab.ticketing.payment.enums.InvoiceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents an invoice issued when a payment is initiated.
 * Tracks what is owed. Transitions to PAID when the linked payment succeeds,
 * or VOID if the payment fails.
 */
@Entity
@Table(name = "invoices")
public class InvoiceEntity {

    @Id
    private UUID id;

    @Column(name = "payment_id", nullable = false, unique = true)
    private UUID paymentId;

    @Column(name = "reservation_id", nullable = false)
    private UUID reservationId;

    @Column(name = "customer_id", nullable = false, length = 200)
    private String customerId;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InvoiceStatus status;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "due_at")
    private Instant dueAt;

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected InvoiceEntity() { }

    public InvoiceEntity(UUID paymentId, UUID reservationId, String customerId,
                         String invoiceNumber, BigDecimal amount, String currency,
                         Instant issuedAt, Instant dueAt) {
        this.paymentId = paymentId;
        this.reservationId = reservationId;
        this.customerId = customerId;
        this.invoiceNumber = invoiceNumber;
        this.amount = amount;
        this.currency = currency;
        this.status = InvoiceStatus.ISSUED;
        this.issuedAt = issuedAt;
        this.dueAt = dueAt;
    }

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getPaymentId() { return paymentId; }
    public UUID getReservationId() { return reservationId; }
    public String getCustomerId() { return customerId; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }
    public Instant getIssuedAt() { return issuedAt; }
    public Instant getDueAt() { return dueAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Instant getCreatedAt() { return createdAt; }
}
