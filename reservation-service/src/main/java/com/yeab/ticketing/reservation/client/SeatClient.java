package com.yeab.ticketing.reservation.client;

import com.yeab.ticketing.reservation.exception.ServiceUnavailableException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * REST client for venue-service's batch seat lookup endpoint.
 */
@Component
public class SeatClient {

    private final RestClient rest;
    private final ObjectMapper objectMapper;

    public SeatClient(@Value("${app.integration.venue-service-url:http://localhost:8082}") String baseUrl,
                      ObjectMapper objectMapper) {
        this.rest = RestClient.builder().baseUrl(baseUrl).build();
        this.objectMapper = objectMapper;
    }

    /**
     * Returns all seats found for the given ids. The remote endpoint does not
     * 404 on missing seats, so missing seats surface as a null {@code section}
     * and are rejected by the caller as "seat unavailable".
     */
    public List<SeatDetails> getSeats(List<UUID> seatIds) {
        try {
            String ids = seatIds.stream().map(UUID::toString).collect(java.util.stream.Collectors.joining(","));
            String raw = rest.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/venues/seats").queryParam("ids", ids).build())
                    .retrieve()
                    .body(String.class);
            JsonNode body = objectMapper.readTree(raw);
            List<SeatDetails> seats = new ArrayList<>();
            for (JsonNode node : body) {
                if (!node.hasNonNull("id")) {
                    continue;
                }
                seats.add(new SeatDetails(
                        UUID.fromString(node.path("id").asText()),
                        UUID.fromString(node.path("venueId").asText()),
                        node.path("section").asText(null)));
            }
            return seats;
        } catch (RestClientException ex) {
            throw new ServiceUnavailableException("Venue service is unavailable", ex);
        } catch (JsonProcessingException ex) {
            throw new ServiceUnavailableException("Could not parse seats from venue service", ex);
        }
    }
}