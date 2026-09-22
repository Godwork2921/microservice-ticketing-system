package com.yeab.ticketing.reservation.entity;

import com.yeab.ticketing.reservation.enums.ReservationStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "reservations")
public class ReservationEntity {
    @Id
    private UUID id;
    @Column(name = "event_id", nullable = false)
    private UUID eventId;
    @Column(name = "customer_id", nullable = false, length = 200)
    private String customerId;
    @Column(name = "customer_email", nullable = false, length = 320)
    private String customerEmail;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReservationStatus status;
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;
    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal discountAmount;
    @Column(name = "discount_code", length = 100)
    private String discountCode;
    @Column(name = "hold_key", length = 200)
    private String holdKey;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "confirmed_at")
    private Instant confirmedAt;
    @Column(name = "hold_expires_at")
    private Instant holdExpiresAt;
    @Version
    private long version;
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationSeatEntity> seats = new ArrayList<>();

    protected ReservationEntity() { }

    public void setId(UUID id) {
        if (this.id == null) {
            this.id = id;
        }
    }

    public ReservationEntity(UUID eventId, String customerId, String customerEmail,
                             BigDecimal totalAmount, String currency, Instant holdExpiresAt) {
        this.eventId = eventId;
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.totalAmount = totalAmount;
        this.discountAmount = BigDecimal.ZERO;
        this.currency = currency;
        this.status = ReservationStatus.HOLD;
        this.holdExpiresAt = holdExpiresAt;
    }

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }

    public void addSeat(ReservationSeatEntity seat) { seats.add(seat); seat.setReservation(this); }
    public UUID getId() { return id; }
    public UUID getEventId() { return eventId; }
    public String getCustomerId() { return customerId; }
    public String getCustomerEmail() { return customerEmail; }
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getCurrency() { return currency; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(Instant confirmedAt) { this.confirmedAt = confirmedAt; }
    public Instant getHoldExpiresAt() { return holdExpiresAt; }
    public List<ReservationSeatEntity> getSeats() { return seats; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public String getDiscountCode() { return discountCode; }
    public String getHoldKey() { return holdKey; }
    public void applyDiscount(String code, BigDecimal amount) {
        this.discountCode = code;
        this.discountAmount = amount == null ? BigDecimal.ZERO : amount;
        this.totalAmount = this.totalAmount.subtract(this.discountAmount).max(BigDecimal.ZERO);
    }
    public void setHoldKey(String holdKey) { this.holdKey = holdKey; }
}
