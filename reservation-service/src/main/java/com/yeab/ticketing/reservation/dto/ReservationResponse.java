package com.yeab.ticketing.reservation.dto;

import com.yeab.ticketing.reservation.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReservationResponse(UUID id, UUID eventId, String customerId, String customerEmail,
                                  ReservationStatus status, BigDecimal totalAmount,
                                  BigDecimal discountAmount, String discountCode, String currency,
                                  Instant createdAt, Instant confirmedAt, Instant holdExpiresAt,
                                  List<ReservationSeatResponse> seats) { }