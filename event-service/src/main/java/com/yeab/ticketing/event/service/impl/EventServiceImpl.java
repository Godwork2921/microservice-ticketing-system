package com.yeab.ticketing.event.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeab.ticketing.event.client.VenueClient;
import com.yeab.ticketing.event.client.VenueUnavailableException;
import com.yeab.ticketing.event.dto.request.CreateEventRequest;
import com.yeab.ticketing.event.dto.request.UpdateEventRequest;
import com.yeab.ticketing.event.dto.response.EventResponse;
import com.yeab.ticketing.event.entity.EventEntity;
import com.yeab.ticketing.event.enums.EventStatus;
import com.yeab.ticketing.event.exception.EventNotFoundException;
import com.yeab.ticketing.event.exception.InvalidEventStateException;
import com.yeab.ticketing.event.exception.InvalidEventTimeException;
import com.yeab.ticketing.event.exception.InvalidPricingRulesException;
import com.yeab.ticketing.event.messaging.EventEventOutbox;
import com.yeab.ticketing.event.repository.EventRepository;
import com.yeab.ticketing.event.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class EventServiceImpl implements EventService {

    private static final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;
    private final VenueClient venueClient;
    private final EventEventOutbox eventOutbox;

    public EventServiceImpl(EventRepository eventRepository, ObjectMapper objectMapper,
                            VenueClient venueClient, EventEventOutbox eventOutbox) {
        this.eventRepository = eventRepository;
        this.objectMapper = objectMapper;
        this.venueClient = venueClient;
        this.eventOutbox = eventOutbox;
    }

    @Override
    public EventResponse create(CreateEventRequest request) {
        validateTimeRange(request.startAt(), request.endAt());
        EventEntity event = new EventEntity(
                request.venueId(),
                request.title().trim(),
                request.startAt(),
                request.endAt(),
                EventStatus.DRAFT,
                normalizePricingRules(request.pricingRules()),
                resolveSeatCount(request.venueId())
        );
        return toResponse(eventRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getById(UUID eventId) {
        return eventRepository.findById(eventId)
                .map(this::toResponse)
                .orElseThrow(() -> new EventNotFoundException(eventId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventResponse> search(String query, Instant from, Instant to, EventStatus status, Pageable pageable) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidEventTimeException();
        }
        String normalizedQuery = query == null ? null : query.trim();
        Specification<EventEntity> specification = (root, criteriaQuery, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (normalizedQuery != null && !normalizedQuery.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        "%" + normalizedQuery.toLowerCase(java.util.Locale.ROOT) + "%"));
            }
            if (from != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startAt"), from));
            }
            if (to != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startAt"), to));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        return eventRepository.findAll(specification, pageable).map(this::toResponse);
    }

    @Override
    public EventResponse update(UUID eventId, UpdateEventRequest request) {
        validateTimeRange(request.startAt(), request.endAt());
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        validateTransition(event.getStatus(), request.status());
        boolean becamePublished = request.status() == EventStatus.PUBLISHED && event.getStatus() != EventStatus.PUBLISHED;
        event.setVenueId(request.venueId());
        event.setTitle(request.title().trim());
        event.setStartAt(request.startAt());
        event.setEndAt(request.endAt());
        event.setStatus(request.status());
        event.setPricingRules(normalizePricingRules(request.pricingRules()));
        EventEntity saved = eventRepository.save(event);
        if (becamePublished) {
            eventOutbox.recordPublished(saved);
        }
        return toResponse(saved);
    }

    private int resolveSeatCount(UUID venueId) {
        try {
            return venueClient.seatCount(venueId);
        } catch (VenueUnavailableException ex) {
            log.warn("Could not resolve seat count for venue {}; defaulting to 0", venueId, ex);
            return 0;
        }
    }

    private void validateTimeRange(Instant startAt, Instant endAt) {
        if (startAt == null || endAt == null || !endAt.isAfter(startAt)) {
            throw new InvalidEventTimeException();
        }
    }

    private void validateTransition(EventStatus current, EventStatus requested) {
        if (current == requested) {
            return;
        }
        boolean valid = switch (current) {
            case DRAFT -> requested == EventStatus.PUBLISHED || requested == EventStatus.CANCELLED;
            case PUBLISHED -> requested == EventStatus.CANCELLED || requested == EventStatus.COMPLETED;
            case CANCELLED, COMPLETED -> false;
        };
        if (!valid) {
            throw new InvalidEventStateException(current, requested);
        }
    }

    private String normalizePricingRules(String pricingRules) {
        if (pricingRules == null || pricingRules.isBlank()) {
            return "{}";
        }
        try {
            JsonNode node = objectMapper.readTree(pricingRules);
            if (node == null || !node.isObject()) {
                throw new InvalidPricingRulesException();
            }
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException exception) {
            throw new InvalidPricingRulesException();
        }
    }

    private EventResponse toResponse(EventEntity event) {
        return new EventResponse(
                event.getId(),
                event.getVenueId(),
                event.getTitle(),
                event.getStartAt(),
                event.getEndAt(),
                event.getStatus(),
                event.getPricingRules(),
                event.getSeatCount(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}
