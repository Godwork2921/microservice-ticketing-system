package com.yeab.ticketing.event.controller;

import com.yeab.ticketing.event.dto.request.CreateEventRequest;
import com.yeab.ticketing.event.dto.request.UpdateEventRequest;
import com.yeab.ticketing.event.dto.response.EventResponse;
import com.yeab.ticketing.event.dto.response.PageResponse;
import com.yeab.ticketing.event.enums.EventStatus;
import com.yeab.ticketing.event.service.EventService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public PageResponse<EventResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) EventStatus status,
            @PageableDefault(size = 20, sort = "startAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return PageResponse.from(eventService.search(q, from, to, status, pageable));
    }

    @GetMapping("/{eventId}")
    public EventResponse getById(@PathVariable UUID eventId) {
        return eventService.getById(eventId);
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public EventResponse create(@Valid @RequestBody CreateEventRequest request) {
        return eventService.create(request);
    }

    @PutMapping("/{eventId}")
    public EventResponse update(@PathVariable UUID eventId, @Valid @RequestBody UpdateEventRequest request) {
        return eventService.update(eventId, request);
    }
}
