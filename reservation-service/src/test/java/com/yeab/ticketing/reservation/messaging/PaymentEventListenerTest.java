package com.yeab.ticketing.reservation.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeab.ticketing.common.events.DomainEventEnvelope;
import com.yeab.ticketing.common.events.EventTypes;
import com.yeab.ticketing.common.events.payload.PaymentFailedEvent;
import com.yeab.ticketing.common.events.payload.PaymentSuccessfulEvent;
import com.yeab.ticketing.reservation.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentEventListenerTest {
    @Mock
    private ReservationService reservationService;

    @Test
    void successfulPaymentConfirmsTheReservation() {
        UUID reservationId = UUID.randomUUID();
        DomainEventEnvelope<?> envelope = DomainEventEnvelope.create(
                EventTypes.PAYMENT_SUCCESSFUL, "corr-1", "payment-service",
                new PaymentSuccessfulEvent(UUID.randomUUID(), reservationId, "c1", "USD",
                        new BigDecimal("20.0000"), "mock", "ref-x"));

        new PaymentEventListener(reservationService, new ObjectMapper().findAndRegisterModules()).onPaymentEvent(envelope);

        verify(reservationService).confirm(reservationId);
        verify(reservationService, never()).cancel(reservationId);
    }

    @Test
    void failedPaymentCancelsTheReservation() {
        UUID reservationId = UUID.randomUUID();
        DomainEventEnvelope<?> envelope = DomainEventEnvelope.create(
                EventTypes.PAYMENT_FAILED, "corr-2", "payment-service",
                new PaymentFailedEvent(UUID.randomUUID(), reservationId, "c1", "USD",
                        new BigDecimal("20.0000"), "mock", "card declined"));

        new PaymentEventListener(reservationService, new ObjectMapper().findAndRegisterModules()).onPaymentEvent(envelope);

        verify(reservationService).cancel(reservationId);
        verify(reservationService, never()).confirm(reservationId);
    }
}