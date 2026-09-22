package com.yeab.ticketing.reservation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reservation_seats")
public class ReservationSeatEntity {
    @Id
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    private ReservationEntity reservation;
    @Column(name = "event_id", nullable = false)
    private UUID eventId;
    @Column(name = "seat_id", nullable = false)
    private UUID seatId;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;
    @Column(nullable = false)
    private boolean released;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ReservationSeatEntity() { }
    public ReservationSeatEntity(UUID eventId, UUID seatId, BigDecimal price) {
        this.eventId = eventId;
        this.seatId = seatId;
        this.price = price;
    }
    @PrePersist
    void onCreate() { if (id == null) id = UUID.randomUUID(); if (createdAt == null) createdAt = Instant.now(); }
    public void setReservation(ReservationEntity reservation) { this.reservation = reservation; }
    public UUID getId() { return id; }
    public UUID getEventId() { return eventId; }
    public UUID getSeatId() { return seatId; }
    public BigDecimal getPrice() { return price; }
    public boolean isReleased() { return released; }
    public void setReleased(boolean released) { this.released = released; }
}
