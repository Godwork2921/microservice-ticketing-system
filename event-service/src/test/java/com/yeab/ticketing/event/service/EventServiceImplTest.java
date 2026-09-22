package com.yeab.ticketing.event.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeab.ticketing.event.client.VenueClient;
import com.yeab.ticketing.event.dto.request.CreateEventRequest;
import com.yeab.ticketing.event.dto.request.UpdateEventRequest;
import com.yeab.ticketing.event.entity.EventEntity;
import com.yeab.ticketing.event.enums.EventStatus;
import com.yeab.ticketing.event.exception.InvalidEventStateException;
import com.yeab.ticketing.event.exception.InvalidEventTimeException;
import com.yeab.ticketing.event.messaging.EventEventOutbox;
import com.yeab.ticketing.event.repository.EventRepository;
import com.yeab.ticketing.event.service.impl.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private VenueClient venueClient;
    @Mock
    private EventEventOutbox eventOutbox;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventServiceImpl(eventRepository, new ObjectMapper(), venueClient, eventOutbox);
    }

    @Test
    void createDefaultsToDraftResolvesSeatCountAndNormalizesPricingRules() {
        UUID venueId = UUID.randomUUID();
        CreateEventRequest request = new CreateEventRequest(
                venueId,
                "  Spring Concert  ",
                Instant.parse("2030-05-01T18:00:00Z"),
                Instant.parse("2030-05-01T20:00:00Z"),
                "{\"adult\": 25.00}"
        );
        when(venueClient.seatCount(venueId)).thenReturn(120);
        when(eventRepository.save(any(EventEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        eventService.create(request);

        ArgumentCaptor<EventEntity> captor = ArgumentCaptor.forClass(EventEntity.class);
        verify(eventRepository).save(captor.capture());
        EventEntity saved = captor.getValue();
        assertThat(saved.getVenueId()).isEqualTo(venueId);
        assertThat(saved.getTitle()).isEqualTo("Spring Concert");
        assertThat(saved.getStatus()).isEqualTo(EventStatus.DRAFT);
        assertThat(saved.getSeatCount()).isEqualTo(120);
        assertThat(saved.getPricingRules()).isEqualTo("{\"adult\":25.0}");
        verify(eventOutbox, never()).recordPublished(any(EventEntity.class));
    }

    @Test
    void publishingAnEventRecordsEventPublishedOutbox() {
        UUID eventId = UUID.randomUUID();
        EventEntity event = new EventEntity(
                UUID.randomUUID(),
                "Published event",
                Instant.parse("2030-05-01T18:00:00Z"),
                Instant.parse("2030-05-01T20:00:00Z"),
                EventStatus.DRAFT,
                "{}",
                120
        );
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(EventEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UpdateEventRequest request = new UpdateEventRequest(
                event.getVenueId(),
                event.getTitle(),
                event.getStartAt(),
                event.getEndAt(),
                EventStatus.PUBLISHED,
                "{}"
        );

        eventService.update(eventId, request);

        verify(eventOutbox).recordPublished(event);
    }

    @Test
    void rejectsAnEventWhoseEndIsNotAfterItsStart() {
        CreateEventRequest request = new CreateEventRequest(
                UUID.randomUUID(),
                "Invalid event",
                Instant.parse("2030-05-01T20:00:00Z"),
                Instant.parse("2030-05-01T18:00:00Z"),
                null
        );

        assertThatThrownBy(() -> eventService.create(request))
                .isInstanceOf(InvalidEventTimeException.class);
    }

    @Test
    void rejectsTerminalEventTransition() {
        UUID eventId = UUID.randomUUID();
        EventEntity event = new EventEntity(
                UUID.randomUUID(),
                "Completed event",
                Instant.parse("2030-05-01T18:00:00Z"),
                Instant.parse("2030-05-01T20:00:00Z"),
                EventStatus.COMPLETED,
                "{}",
                0
        );
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        UpdateEventRequest request = new UpdateEventRequest(
                event.getVenueId(),
                event.getTitle(),
                event.getStartAt(),
                event.getEndAt(),
                EventStatus.CANCELLED,
                "{}"
        );

        assertThatThrownBy(() -> eventService.update(eventId, request))
                .isInstanceOf(InvalidEventStateException.class);
    }
}
