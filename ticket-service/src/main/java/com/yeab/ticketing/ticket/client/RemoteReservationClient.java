package com.yeab.ticketing.ticket.client;

import com.yeab.ticketing.ticket.exception.TicketGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class RemoteReservationClient implements ReservationClient {

    private final RestClient rest;
    private final ObjectMapper objectMapper;

    public RemoteReservationClient(@Value("${app.integration.reservation-service-url:http://localhost:8083}") String baseUrl,
                                   ObjectMapper objectMapper) {
        this.rest = RestClient.builder().baseUrl(baseUrl).build();
        this.objectMapper = objectMapper;
    }

    @Override
    public ReservationDetails getReservation(UUID reservationId) {
        try {
            String raw = rest.get()
                    .uri("/api/reservations/{id}", reservationId)
                    .retrieve()
                    .body(String.class);
            JsonNode body = objectMapper.readTree(raw);
            List<UUID> seatIds = new ArrayList<>();
            JsonNode seats = body.path("seats");
            if (seats.isArray()) {
                seats.forEach(seat -> seatIds.add(UUID.fromString(seat.path("seatId").asText())));
            }
            return new ReservationDetails(
                    reservationId,
                    body.path("status").asText(null),
                    body.hasNonNull("eventId") ? UUID.fromString(body.path("eventId").asText()) : null,
                    body.path("customerId").asText(null),
                    body.path("customerEmail").asText(null),
                    seatIds,
                    body.hasNonNull("totalAmount") ? new BigDecimal(body.path("totalAmount").asText()) : null);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new TicketGenerationException("Reservation " + reservationId + " not found");
        } catch (RestClientException ex) {
            throw new TicketGenerationException("Reservation service unavailable");
        } catch (JsonProcessingException ex) {
            throw new TicketGenerationException("Could not parse reservation " + reservationId + " response");
        }
    }
}