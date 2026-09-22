package com.yeab.ticketing.payment.client;

import com.yeab.ticketing.payment.exception.PaymentNotFoundException;
import com.yeab.ticketing.payment.exception.ServiceUnavailableException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
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
            return new ReservationDetails(
                    reservationId,
                    body.path("status").asText(null),
                    body.hasNonNull("totalAmount") ? new BigDecimal(body.path("totalAmount").asText()) : null,
                    body.path("currency").asText(null),
                    body.path("customerId").asText(null),
                    body.path("customerEmail").asText(null));
        } catch (HttpClientErrorException.NotFound ex) {
            throw new PaymentNotFoundException("Reservation " + reservationId + " not found: " + ex.getMessage());
        } catch (RestClientException ex) {
            throw new ServiceUnavailableException("Reservation service unavailable", ex);
        } catch (JsonProcessingException ex) {
            throw new ServiceUnavailableException("Could not parse reservation " + reservationId + " response", ex);
        }
    }
}