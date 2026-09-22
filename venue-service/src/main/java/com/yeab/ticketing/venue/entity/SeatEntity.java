package com.yeab.ticketing.venue.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "seats")
public class SeatEntity {

    @Id
    private UUID id;
    @Column(name = "venue_id", nullable = false)
    private UUID venueId;
    @Column(nullable = false, length = 100)
    private String section;
    @Column(name = "seat_row", nullable = false, length = 50)
    private String seatRow;
    @Column(name = "seat_number", nullable = false)
    private int number;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String attributes;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected SeatEntity() {
    }

    public SeatEntity(UUID venueId, String section, String seatRow, int number, String attributes) {
        this.venueId = venueId;
        this.section = section;
        this.seatRow = seatRow;
        this.number = number;
        this.attributes = attributes;
    }

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getVenueId() { return venueId; }
    public String getSection() { return section; }
    public String getSeatRow() { return seatRow; }
    public int getNumber() { return number; }
    public String getAttributes() { return attributes; }
    public Instant getCreatedAt() { return createdAt; }
}
