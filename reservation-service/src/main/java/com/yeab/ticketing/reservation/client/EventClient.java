package com.yeab.ticketing.reservation.client;

import com.yeab.ticketing.reservation.exception.EventNotAvailableException;
import com.yeab.ticketing.reservation.exception.ServiceUnavailableException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

/**
 * REST client for event-service. Synchronous request-response is the correct
 * pattern here: the hold flow needs the event's venue id and server-side pricing
 * rules before pricing a seat, and this data must be fresh.
 */
@Component
public class EventClient {

    private final RestClient rest;
    private final ObjectMapper objectMapper;
    @Value("${app.integration.event-service-url:http://localhost:8081}")
    private String baseUrl;

    public EventClient(@Value("${app.integration.event-service-url:http://localhost:8081}") String baseUrl,
                       ObjectMapper objectMapper) {
        this.baseUrl = baseUrl;
        this.rest = RestClient.builder().baseUrl(baseUrl).build();
        this.objectMapper = objectMapper;
    }

    public EventDetails getEvent(UUID eventId) {
        try {
            String raw = rest.get()
                    .uri("/api/events/{id}", eventId)
                    .retrieve()
                    .body(String.class);
            JsonNode body = objectMapper.readTree(raw);
            return new EventDetails(eventId,
                    UUID.fromString(body.path("venueId").asText()),
                    body.path("status").asText(null),
                    body.path("pricingRules").asText("{}"));
        } catch (HttpClientErrorException.NotFound ex) {
            throw new EventNotAvailableException("Event " + eventId + " was not found", ex);
        } catch (RestClientException ex) {
            throw new ServiceUnavailableException("Event service is unavailable at " + baseUrl, ex);
        } catch (JsonProcessingException ex) {
            throw new ServiceUnavailableException("Could not parse event from event service", ex);
        }
    }
}