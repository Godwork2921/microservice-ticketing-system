package com.yeab.ticketing.reservation.mapper;

import com.yeab.ticketing.reservation.dto.ReservationResponse;
import com.yeab.ticketing.reservation.dto.ReservationSeatResponse;
import com.yeab.ticketing.reservation.entity.ReservationEntity;

public final class ReservationMapper {
    private ReservationMapper() { }

    public static ReservationResponse toResponse(ReservationEntity entity) {
        return new ReservationResponse(entity.getId(), entity.getEventId(), entity.getCustomerId(),
                entity.getCustomerEmail(), entity.getStatus(), entity.getTotalAmount(),
                entity.getDiscountAmount(), entity.getDiscountCode(), entity.getCurrency(),
                entity.getCreatedAt(), entity.getConfirmedAt(), entity.getHoldExpiresAt(),
                entity.getSeats().stream().map(seat -> new ReservationSeatResponse(
                        seat.getSeatId(), seat.getPrice(), seat.isReleased())).toList());
    }
}