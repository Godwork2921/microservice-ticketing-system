package com.yeab.ticketing.event.service;

import com.yeab.ticketing.event.dto.request.CreateEventRequest;
import com.yeab.ticketing.event.dto.request.UpdateEventRequest;
import com.yeab.ticketing.event.dto.response.EventResponse;
import com.yeab.ticketing.event.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

public interface EventService {

    EventResponse create(CreateEventRequest request);

    EventResponse getById(UUID eventId);

    Page<EventResponse> search(String query, Instant from, Instant to, EventStatus status, Pageable pageable);

    EventResponse update(UUID eventId, UpdateEventRequest request);
}
