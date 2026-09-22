package com.yeab.ticketing.event.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
public class RemoteVenueClient implements VenueClient {

    private final RestClient rest;
    private final ObjectMapper objectMapper;

    public RemoteVenueClient(@Value("${app.integration.venue-service-url:http://localhost:8082}") String baseUrl,
                             ObjectMapper objectMapper) {
        this.rest = RestClient.builder().baseUrl(baseUrl).build();
        this.objectMapper = objectMapper;
    }

    @Override
    public int seatCount(UUID venueId) {
        try {
            String raw = rest.get()
                    .uri("/api/venues/{venueId}/seats/count", venueId)
                    .retrieve()
                    .body(String.class);
            JsonNode body = objectMapper.readTree(raw);
            return body == null ? 0 : body.path("count").asInt(0);
        } catch (RestClientException ex) {
            throw new VenueUnavailableException("Venue service unavailable for " + venueId, ex);
        } catch (JsonProcessingException ex) {
            throw new VenueUnavailableException("Could not parse venue seat count for " + venueId, ex);
        }
    }
}