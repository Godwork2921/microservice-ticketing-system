package com.yeab.ticketing.ticket.entity;

import com.yeab.ticketing.ticket.enums.TicketStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tickets")
public class TicketEntity {
    @Id private UUID id;
    @Column(name = "ticket_code", nullable = false, unique = true, length = 100) private String ticketCode;
    @Column(name = "reservation_id", nullable = false) private UUID reservationId;
    @Column(name = "event_id", nullable = false) private UUID eventId;
    @Column(name = "customer_id", nullable = false, length = 200) private String customerId;
    @Column(name = "customer_email", length = 320) private String customerEmail;
    @Column(name = "seat_id", nullable = false) private UUID seatId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private TicketStatus status;
    @Column(name = "issued_at", nullable = false, updatable = false) private Instant issuedAt;
    @Column(name = "used_at") private Instant usedAt;
    @Column(name = "cancelled_at") private Instant cancelledAt;
    @Column(name = "storage_object_key", length = 500) private String storageObjectKey;
    @Version private long version;
    protected TicketEntity() { }
    public TicketEntity(UUID reservationId, UUID eventId, String customerId, String customerEmail, UUID seatId) {
        this.ticketCode = "TKT-" + UUID.randomUUID(); this.reservationId = reservationId; this.eventId = eventId;
        this.customerId = customerId; this.customerEmail = customerEmail; this.seatId = seatId; this.status = TicketStatus.ISSUED;
    }
    @PrePersist void onCreate() { if (id == null) id = UUID.randomUUID(); if (issuedAt == null) issuedAt = Instant.now(); }
    public UUID getId() { return id; }
    public void setId(UUID id) { if (this.id == null) this.id = id; }
    public String getTicketCode() { return ticketCode; }
    public UUID getReservationId() { return reservationId; } public UUID getEventId() { return eventId; }
    public String getCustomerId() { return customerId; } public String getCustomerEmail() { return customerEmail; }
    public UUID getSeatId() { return seatId; }
    public TicketStatus getStatus() { return status; } public void setStatus(TicketStatus status) { this.status = status; }
    public Instant getIssuedAt() { return issuedAt; } public Instant getUsedAt() { return usedAt; }
    public void setUsedAt(Instant usedAt) { this.usedAt = usedAt; } public Instant getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; }
    public String getStorageObjectKey() { return storageObjectKey; }
    public void setStorageObjectKey(String storageObjectKey) { this.storageObjectKey = storageObjectKey; }
}