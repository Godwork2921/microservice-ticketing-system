package com.yeab.ticketing.reservation.messaging;

import com.yeab.ticketing.common.events.DomainEventEnvelope;
import com.yeab.ticketing.common.events.EventTypes;
import com.yeab.ticketing.common.events.KafkaTopics;
import com.yeab.ticketing.reservation.exception.InvalidReservationStateException;
import com.yeab.ticketing.reservation.exception.ReservationExpiredException;
import com.yeab.ticketing.reservation.exception.ReservationNotFoundException;
import com.yeab.ticketing.reservation.service.ReservationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Closes the purchase loop: a successful payment confirms the held reservation;
 * a failed one cancels it and releases the seats. State transitions are the same
 * atomic CAS operations used by the REST API, so replays are naturally ignored.
 */
@Component
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    private final ReservationService reservationService;
    private final ObjectMapper objectMapper;

    public PaymentEventListener(ReservationService reservationService, ObjectMapper objectMapper) {
        this.reservationService = reservationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_EVENTS,
            groupId = "${spring.kafka.consumer.group-id:reservation-service}")
    public void onPaymentEvent(DomainEventEnvelope envelope) {
        UUID reservationId = reservationIdOf(envelope);
        if (reservationId == null) {
            log.warn("Payment event {} arrived without a reservationId", envelope.eventType());
            return;
        }
        switch (envelope.eventType()) {
            case EventTypes.PAYMENT_SUCCESSFUL -> confirmFromPayment(reservationId);
            case EventTypes.PAYMENT_FAILED -> cancelFromPayment(reservationId);
            default -> log.debug("Ignoring payment event {}", envelope.eventType());
        }
    }

    private void confirmFromPayment(UUID reservationId) {
        try {
            reservationService.confirm(reservationId);
            log.info("Reservation {} confirmed via payment.successful", reservationId);
        } catch (ReservationExpiredException | InvalidReservationStateException ex) {
            log.warn("Could not confirm reservation {} from payment event: {}", reservationId, ex.getMessage());
        } catch (ReservationNotFoundException ex) {
            log.error("Payment event referenced unknown reservation {}", reservationId);
        }
    }

    private void cancelFromPayment(UUID reservationId) {
        try {
            reservationService.cancel(reservationId);
            log.info("Reservation {} cancelled via payment.failed", reservationId);
        } catch (InvalidReservationStateException ex) {
            log.warn("Could not cancel reservation {} from payment event: {}", reservationId, ex.getMessage());
        }
    }

    /**
     * Extracts the reservation id from the payload, which arrives either as a
     * {@code LinkedHashMap} (when consumed from Kafka) or as a typed record
     * (when constructed directly in tests).
     */
    private UUID reservationIdOf(DomainEventEnvelope envelope) {
        try {
            JsonNode node = objectMapper.valueToTree(envelope.payload());
            JsonNode value = node.get("reservationId");
            return value == null || value.isNull() ? null : UUID.fromString(value.asText());
        } catch (Exception ex) {
            log.warn("Could not extract reservationId from payload", ex);
            return null;
        }
    }
}