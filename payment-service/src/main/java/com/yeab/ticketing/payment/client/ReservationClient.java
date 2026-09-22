package com.yeab.ticketing.payment.client;

import java.util.UUID;

public interface ReservationClient {

    /**
     * Fetches a reservation so the amount is always derived server-side.
     *
     * @throws com.yeab.ticketing.payment.exception.PaymentNotFoundException
     *         when the reservation does not exist remotely
     * @throws com.yeab.ticketing.payment.exception.ServiceUnavailableException
     *         when reservation-service cannot be reached
     */
    ReservationDetails getReservation(UUID reservationId);
}