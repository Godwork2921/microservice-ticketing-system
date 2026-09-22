package com.yeab.ticketing.reservation.service;

import com.yeab.ticketing.reservation.dto.HoldReservationRequest;
import com.yeab.ticketing.reservation.dto.ReservationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReservationService {
    ReservationResponse hold(HoldReservationRequest request, String idempotencyKey);
    ReservationResponse getById(UUID reservationId);
    Page<ReservationResponse> list(String customerId, Pageable pageable);
    ReservationResponse confirm(UUID reservationId);
    ReservationResponse cancel(UUID reservationId);
}