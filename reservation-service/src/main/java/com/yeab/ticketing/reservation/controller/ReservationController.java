package com.yeab.ticketing.reservation.controller;

import com.yeab.ticketing.reservation.dto.HoldReservationRequest;
import com.yeab.ticketing.reservation.dto.ReservationResponse;
import com.yeab.ticketing.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Reservations", description = "Hold, confirm and cancel seat reservations")
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @Operation(summary = "Hold seats", description = "Atomically holds seats for an event. " +
            "Prices are computed server-side. Re-submitting the same Idempotency-Key returns the original hold.")
    @PostMapping("/hold")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse hold(@Valid @RequestBody HoldReservationRequest request,
                                    @Parameter(description = "Optional client idempotency key")
                                    @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return service.hold(request, idempotencyKey);
    }

    @Operation(summary = "Get reservation by id")
    @GetMapping("/{reservationId}")
    public ReservationResponse getById(@PathVariable UUID reservationId) {
        return service.getById(reservationId);
    }

    @Operation(summary = "List reservations for a customer")
    @GetMapping
    public Page<ReservationResponse> list(@RequestParam(required = false) String customerId,
                                          @PageableDefault(size = 20) Pageable pageable) {
        return service.list(customerId, pageable);
    }

    @Operation(summary = "Confirm a held reservation", description = "Fails with 409 if the hold expired or the state is invalid.")
    @PostMapping("/{reservationId}/confirm")
    public ReservationResponse confirm(@PathVariable UUID reservationId) {
        return service.confirm(reservationId);
    }

    @Operation(summary = "Cancel a reservation")
    @DeleteMapping("/{reservationId}/cancel")
    public ReservationResponse cancel(@PathVariable UUID reservationId) {
        return service.cancel(reservationId);
    }
}