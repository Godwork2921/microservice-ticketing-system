package com.yeab.ticketing.ticket.client;

import java.util.UUID;

public interface ReservationClient {

    ReservationDetails getReservation(UUID reservationId);
}