package com.yeab.ticketing.ticket.service;

import com.yeab.ticketing.ticket.dto.IssueTicketsRequest;
import com.yeab.ticketing.ticket.entity.TicketEntity;
import com.yeab.ticketing.ticket.enums.TicketStatus;
import com.yeab.ticketing.ticket.messaging.TicketEventOutbox;
import com.yeab.ticketing.ticket.qr.QrCodeService;
import com.yeab.ticketing.ticket.repository.TicketRepository;
import com.yeab.ticketing.ticket.storage.TicketObjectStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {
    @Mock private TicketRepository repository;
    @Mock private TicketEventOutbox eventOutbox;
    @Mock private QrCodeService qrCodeService;
    @Mock private TicketObjectStorage storage;
    private TicketServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TicketServiceImpl(repository, eventOutbox, qrCodeService, storage,
                Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));
    }

    private TicketEntity issuedTicket(UUID ticketId) {
        TicketEntity ticket = new TicketEntity(UUID.randomUUID(), UUID.randomUUID(), "customer-1", null, UUID.randomUUID());
        ticket.setId(ticketId);
        return ticket;
    }

    @Test void issueIsIdempotentByReservation() {
        UUID reservationId = UUID.randomUUID();
        TicketEntity existing = issuedTicket(UUID.randomUUID());
        when(repository.findByReservationId(reservationId)).thenReturn(List.of(existing));
        var result = service.issue(new IssueTicketsRequest(reservationId, UUID.randomUUID(), "customer-1", List.of(UUID.randomUUID())));
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().ticketCode()).isEqualTo(existing.getTicketCode());
    }

    @Test void useMarksIssuedTicketUsed() {
        UUID id = UUID.randomUUID();
        TicketEntity ticket = issuedTicket(id);
        when(repository.findById(id)).thenReturn(Optional.of(ticket));
        var result = service.use(id);
        assertThat(result.status()).isEqualTo(TicketStatus.USED);
        assertThat(result.usedAt()).isNotNull();
    }
}