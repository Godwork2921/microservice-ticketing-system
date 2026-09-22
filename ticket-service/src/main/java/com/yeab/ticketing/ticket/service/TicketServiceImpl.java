package com.yeab.ticketing.ticket.service;

import com.yeab.ticketing.ticket.client.ReservationDetails;
import com.yeab.ticketing.ticket.dto.IssueTicketsRequest;
import com.yeab.ticketing.ticket.dto.TicketResponse;
import com.yeab.ticketing.ticket.entity.TicketEntity;
import com.yeab.ticketing.ticket.enums.TicketStatus;
import com.yeab.ticketing.ticket.exception.InvalidTicketStateException;
import com.yeab.ticketing.ticket.exception.TicketGenerationException;
import com.yeab.ticketing.ticket.exception.TicketNotFoundException;
import com.yeab.ticketing.ticket.mapper.TicketMapper;
import com.yeab.ticketing.ticket.messaging.TicketEventOutbox;
import com.yeab.ticketing.ticket.qr.QrCodeService;
import com.yeab.ticketing.ticket.repository.TicketRepository;
import com.yeab.ticketing.ticket.storage.TicketObjectStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TicketServiceImpl implements TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketServiceImpl.class);

    private final TicketRepository repository;
    private final TicketEventOutbox eventOutbox;
    private final QrCodeService qrCodeService;
    private final TicketObjectStorage storage;
    private final Clock clock;

    public TicketServiceImpl(TicketRepository repository, TicketEventOutbox eventOutbox,
                             QrCodeService qrCodeService, TicketObjectStorage storage, Clock clock) {
        this.repository = repository;
        this.eventOutbox = eventOutbox;
        this.qrCodeService = qrCodeService;
        this.storage = storage;
        this.clock = clock;
    }

    @Override
    @Transactional
    public List<TicketResponse> issue(IssueTicketsRequest request) {
        List<TicketEntity> existing = repository.findByReservationId(request.reservationId());
        if (!existing.isEmpty()) {
            return existing.stream().map(TicketMapper::toResponse).toList();
        }
        if (request.seatIds().stream().distinct().count() != request.seatIds().size()) {
            throw new IllegalArgumentException("Duplicate seat IDs are not allowed");
        }
        List<TicketEntity> created = new ArrayList<>();
        for (UUID seatId : request.seatIds()) {
            TicketEntity ticket = new TicketEntity(request.reservationId(), request.eventId(),
                    request.customerId().trim(), null, seatId);
            attachQr(ticket);
            created.add(repository.save(ticket));
            eventOutbox.recordGenerated(ticket.getId(), ticket.getReservationId(), ticket.getEventId(),
                    ticket.getSeatId(), ticket.getCustomerId(), ticket.getCustomerEmail(),
                    ticket.getTicketCode(), ticket.getStorageObjectKey());
        }
        return created.stream().map(TicketMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public List<TicketResponse> generateForReservation(ReservationDetails reservation) {
        List<TicketEntity> existing = repository.findByReservationId(reservation.id());
        if (!existing.isEmpty()) {
            log.info("Tickets already exist for reservation {}, skipping", reservation.id());
            return existing.stream().map(TicketMapper::toResponse).toList();
        }
        if (reservation.seatIds() == null || reservation.seatIds().isEmpty()) {
            throw new TicketGenerationException("Reservation " + reservation.id() + " has no seats");
        }
        try {
            List<TicketEntity> created = new ArrayList<>();
            for (UUID seatId : reservation.seatIds()) {
                TicketEntity ticket = new TicketEntity(reservation.id(), reservation.eventId(),
                        reservation.customerId(), reservation.customerEmail(), seatId);
                attachQr(ticket);
                created.add(repository.save(ticket));
            }
            repository.flush();
            for (TicketEntity ticket : created) {
                eventOutbox.recordGenerated(ticket.getId(), ticket.getReservationId(), ticket.getEventId(),
                        ticket.getSeatId(), ticket.getCustomerId(), ticket.getCustomerEmail(),
                        ticket.getTicketCode(), ticket.getStorageObjectKey());
            }
            recordDeliveryRequest(created, reservation);
            log.info("Generated {} tickets for reservation {}", created.size(), reservation.id());
            return created.stream().map(TicketMapper::toResponse).toList();
        } catch (DataIntegrityViolationException ex) {
            // Another consumer issued these tickets concurrently; return the winners.
            return repository.findByReservationId(reservation.id())
                    .stream().map(TicketMapper::toResponse).toList();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getById(UUID id) {
        return TicketMapper.toResponse(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> byReservation(UUID id) {
        return repository.findByReservationId(id).stream().map(TicketMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> list(String customerId, Pageable pageable) {
        return (customerId == null || customerId.isBlank()
                ? repository.findAll(pageable)
                : repository.findByCustomerIdOrderByIssuedAtDesc(customerId, pageable))
                .map(TicketMapper::toResponse);
    }

    @Override
    @Transactional
    public TicketResponse use(UUID id) {
        TicketEntity ticket = find(id);
        require(ticket, TicketStatus.ISSUED);
        ticket.setStatus(TicketStatus.USED);
        ticket.setUsedAt(clock.instant());
        return TicketMapper.toResponse(ticket);
    }

    @Override
    @Transactional
    public TicketResponse cancel(UUID id) {
        TicketEntity ticket = find(id);
        require(ticket, TicketStatus.ISSUED);
        ticket.setStatus(TicketStatus.CANCELLED);
        ticket.setCancelledAt(clock.instant());
        return TicketMapper.toResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse validate(String ticketCode) {
        TicketEntity ticket = repository.findByTicketCode(ticketCode.trim())
                .orElseThrow(() -> new TicketNotFoundException("Ticket with code " + ticketCode + " not found"));
        if (ticket.getStatus() != TicketStatus.ISSUED) {
            throw new InvalidTicketStateException(
                    "Ticket is not valid for entry (status=" + ticket.getStatus() + ")");
        }
        return TicketMapper.toResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] qrImage(UUID id) {
        find(id);
        try {
            return storage.get(id);
        } catch (IOException ex) {
            throw new TicketGenerationException("Could not load QR image for ticket " + id);
        }
    }

    private TicketEntity find(UUID id) {
        return repository.findById(id).orElseThrow(() -> new TicketNotFoundException(id));
    }

    private void require(TicketEntity ticket, TicketStatus expected) {
        if (ticket.getStatus() != expected) {
            throw new InvalidTicketStateException("Ticket must be " + expected + " but is " + ticket.getStatus());
        }
    }

    private void attachQr(TicketEntity ticket) {
        if (ticket.getId() == null) {
            ticket.setId(UUID.randomUUID());
        }
        byte[] png = qrCodeService.generatePng(ticket.getTicketCode());
        try {
            ticket.setStorageObjectKey(storage.store(ticket.getId(), png));
        } catch (IOException ex) {
            throw new TicketGenerationException("Could not store QR image for ticket " + ticket.getId());
        }
    }

    private void recordDeliveryRequest(List<TicketEntity> created, ReservationDetails reservation) {
        if (created.isEmpty() || reservation.customerEmail() == null || reservation.customerEmail().isBlank()) {
            return;
        }
        String subject = "Your tickets are ready";
        String message = "Payment confirmed. Your " + created.size()
                + " ticket(s) for event " + reservation.eventId()
                + " are ready (reservation " + reservation.id() + ").";
        eventOutbox.recordRequested("ticket.delivery." + reservation.id(), reservation.customerId(),
                "EMAIL", reservation.customerEmail(), subject, message);
    }
}