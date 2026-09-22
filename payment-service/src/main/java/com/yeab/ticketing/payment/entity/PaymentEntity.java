package com.yeab.ticketing.payment.entity;

import com.yeab.ticketing.payment.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class PaymentEntity {
    @Id
    private UUID id;
    @Column(name = "reservation_id", nullable = false, unique = true)
    private UUID reservationId;
    @Column(name = "customer_id", nullable = false, length = 200)
    private String customerId;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;
    @Column(nullable = false, length = 3)
    private String currency;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;
    @Column(nullable = false, length = 50)
    private String provider;
    @Column(name = "provider_payment_id", length = 200)
    private String providerPaymentId;
    @Column(name = "checkout_url", length = 1000)
    private String checkoutUrl;
    @Column(name = "failure_reason", length = 500)
    private String failureReason;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Column(name = "completed_at")
    private Instant completedAt;
    @Version
    private long version;

    protected PaymentEntity() { }

    public PaymentEntity(UUID reservationId, String customerId, BigDecimal amount, String currency, String provider) {
        this.reservationId = reservationId;
        this.customerId = customerId;
        this.amount = amount;
        this.currency = currency;
        this.provider = provider;
        this.status = PaymentStatus.PENDING;
    }

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }

    public void setId(UUID id) {
        if (this.id == null) {
            this.id = id;
        }
    }
    public UUID getReservationId() { return reservationId; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public String getProvider() { return provider; }
    public String getProviderPaymentId() { return providerPaymentId; }
    public void setProviderPaymentId(String providerPaymentId) { this.providerPaymentId = providerPaymentId; }
    public String getCheckoutUrl() { return checkoutUrl; }
    public void setCheckoutUrl(String checkoutUrl) { this.checkoutUrl = checkoutUrl; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
