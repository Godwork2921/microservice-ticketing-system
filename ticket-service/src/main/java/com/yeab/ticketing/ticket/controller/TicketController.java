package com.yeab.ticketing.ticket.controller;

import com.yeab.ticketing.ticket.dto.IssueTicketsRequest;
import com.yeab.ticketing.ticket.dto.TicketResponse;
import com.yeab.ticketing.ticket.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Tickets", description = "Issue, validate and download tickets")
@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @Operation(summary = "Issue tickets (dev path)", description = "The production flow is payment-gated via Kafka.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<TicketResponse> issue(@Valid @RequestBody IssueTicketsRequest request) {
        return service.issue(request);
    }

    @Operation(summary = "List tickets")
    @GetMapping
    public Page<TicketResponse> list(@RequestParam(required = false) String customerId,
                                     @PageableDefault(size = 20) Pageable pageable) {
        return service.list(customerId, pageable);
    }

    @Operation(summary = "Get ticket by id")
    @GetMapping("/{id}")
    public TicketResponse get(@PathVariable UUID id) {
        return service.getById(id);
    }

    @Operation(summary = "Get tickets by reservation")
    @GetMapping("/reservation/{reservationId}")
    public List<TicketResponse> reservation(@PathVariable UUID reservationId) {
        return service.byReservation(reservationId);
    }

    @Operation(summary = "Validate a ticket at the gate", description = "Only ISSUED tickets are valid.")
    @PostMapping("/validate")
    public TicketResponse validate(@RequestParam String ticketCode) {
        return service.validate(ticketCode);
    }

    @Operation(summary = "Download the QR image for a ticket")
    @GetMapping("/{id}/qr")
    public ResponseEntity<byte[]> qr(@PathVariable UUID id) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(service.qrImage(id));
    }

    @Operation(summary = "Mark a ticket as used")
    @PostMapping("/{id}/use")
    public TicketResponse use(@PathVariable UUID id) {
        return service.use(id);
    }

    @Operation(summary = "Cancel a ticket")
    @PostMapping("/{id}/cancel")
    public TicketResponse cancel(@PathVariable UUID id) {
        return service.cancel(id);
    }
}