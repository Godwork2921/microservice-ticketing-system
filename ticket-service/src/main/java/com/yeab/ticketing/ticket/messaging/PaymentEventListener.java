package com.yeab.ticketing.ticket.messaging;

import com.yeab.ticketing.common.events.DomainEventEnvelope;
import com.yeab.ticketing.common.events.EventTypes;
import com.yeab.ticketing.common.events.KafkaTopics;
import com.yeab.ticketing.ticket.client.ReservationClient;
import com.yeab.ticketing.ticket.client.ReservationDetails;
import com.yeab.ticketing.ticket.exception.TicketGenerationException;
import com.yeab.ticketing.ticket.service.TicketService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Produces tickets once payment succeeds. Ticket generation is payment-gated:
 * the reservation must have reached CONFIRMED (payment.successful triggers its
 * confirmation in reservation-service), which this listener waits for with a
 * bounded retry to absorb the cross-service propagation delay.
 */
@Component
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    private final TicketService ticketService;
    private final ReservationClient reservationClient;
    private final ObjectMapper objectMapper;
    private final int confirmAttempts;
    private final long confirmDelayMs;

    public PaymentEventListener(TicketService ticketService,
                                ReservationClient reservationClient,
                                ObjectMapper objectMapper,
                                @Value("${app.ticket.confirm-attempts:10}") int confirmAttempts,
                                @Value("${app.ticket.confirm-delay-ms:500}") long confirmDelayMs) {
        this.ticketService = ticketService;
        this.reservationClient = reservationClient;
        this.objectMapper = objectMapper;
        this.confirmAttempts = confirmAttempts;
        this.confirmDelayMs = confirmDelayMs;
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_EVENTS,
            groupId = "${spring.kafka.consumer.group-id:ticket-service}")
    public void onPaymentEvent(DomainEventEnvelope envelope) {
        if (!EventTypes.PAYMENT_SUCCESSFUL.equals(envelope.eventType())) {
            return;
        }
        UUID reservationId = reservationIdOf(envelope);
        if (reservationId == null) {
            log.warn("payment.successful event without a reservationId; ignoring");
            return;
        }
        ReservationDetails reservation = awaitConfirmed(reservationId);
        if (reservation == null) {
            log.error("Reservation {} never reached CONFIRMED; not generating tickets", reservationId);
            return;
        }
        ticketService.generateForReservation(reservation);
    }

    /**
     * Waits for the reservation to be CONFIRMED before issuing tickets. A few
     * early attempts absorb the delay between payment.successful being published
     * and reservation-service confirming the hold. If it still is not confirmed
     * (e.g. the hold expired before the payment was processed), no tickets are
     * generated and the payment is treated as non-settlable.
     */
    private ReservationDetails awaitConfirmed(UUID reservationId) {
        for (int attempt = 1; attempt <= confirmAttempts; attempt++) {
            try {
                ReservationDetails reservation = reservationClient.getReservation(reservationId);
                if ("CONFIRMED".equals(reservation.status())) {
                    return reservation;
                }
                log.debug("Reservation {} not yet CONFIRMED (status={}, attempt {}/{})",
                        reservationId, reservation.status(), attempt, confirmAttempts);
            } catch (TicketGenerationException ex) {
                log.warn("Could not load reservation {} (attempt {}/{})", reservationId, attempt, confirmAttempts);
            }
            if (attempt < confirmAttempts) {
                try {
                    Thread.sleep(confirmDelayMs);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        return null;
    }

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