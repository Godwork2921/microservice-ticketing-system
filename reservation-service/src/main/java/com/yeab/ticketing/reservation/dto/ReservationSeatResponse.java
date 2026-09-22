package com.yeab.ticketing.reservation.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ReservationSeatResponse(UUID seatId, BigDecimal price, boolean released) { }
