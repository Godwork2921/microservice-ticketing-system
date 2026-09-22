package com.yeab.ticketing.reservation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeab.ticketing.reservation.client.EventClient;
import com.yeab.ticketing.reservation.client.EventDetails;
import com.yeab.ticketing.reservation.client.SeatClient;
import com.yeab.ticketing.reservation.client.SeatDetails;
import com.yeab.ticketing.reservation.config.ReservationProperties;
import com.yeab.ticketing.reservation.dto.HoldReservationRequest;
import com.yeab.ticketing.reservation.entity.ReservationEntity;
import com.yeab.ticketing.reservation.entity.ReservationSeatEntity;
import com.yeab.ticketing.reservation.enums.ReservationStatus;
import com.yeab.ticketing.reservation.exception.EventNotBookableException;
import com.yeab.ticketing.reservation.exception.ReservationExpiredException;
import com.yeab.ticketing.reservation.messaging.ReservationEventOutbox;
import com.yeab.ticketing.reservation.repository.OutboxRepository;
import com.yeab.ticketing.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {
    @Mock
    private ReservationRepository repository;
    @Mock
    private OutboxRepository outboxRepository;
    @Mock
    private EventClient eventClient;
    @Mock
    private SeatClient seatClient;

    private ReservationServiceImpl service;
    private ReservationProperties properties;
    private UUID eventId;
    private UUID venueId;
    private UUID vipSeatId;
    private UUID standardSeatId;

    @BeforeEach
    void setUp() {
        properties = new ReservationProperties();
        properties.setHoldDuration(Duration.ofMinutes(10));
        ReservationProperties.DiscountDefinition flat5 = new ReservationProperties.DiscountDefinition();
        flat5.setType(ReservationProperties.DiscountType.FIXED);
        flat5.setValue(new BigDecimal("5"));
        properties.getDiscounts().put("FLAT5", flat5);

        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        service = new ReservationServiceImpl(repository,
                eventClient,
                seatClient,
                new PricingService(mapper),
                new DiscountService(properties),
                new ReservationEventOutbox(outboxRepository, mapper),
                properties,
                Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));

        eventId = UUID.randomUUID();
        venueId = UUID.randomUUID();
        vipSeatId = UUID.randomUUID();
        standardSeatId = UUID.randomUUID();
    }

    @Test
    void holdPricesServerSideAndAppliesDiscount() {
        when(eventClient.getEvent(eventId)).thenReturn(new EventDetails(eventId, venueId, "PUBLISHED",
                "{\"VIP\":100,\"DEFAULT\":50}"));
        when(seatClient.getSeats(any())).thenReturn(List.of(
                new SeatDetails(vipSeatId, venueId, "VIP"),
                new SeatDetails(standardSeatId, venueId, "STANDARD")));
        when(repository.saveAndFlush(any(ReservationEntity.class))).thenAnswer(invocation -> {
            ReservationEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        var response = service.hold(request("FLAT5"), "hold-1");

        assertThat(response.eventId()).isEqualTo(eventId);
        assertThat(response.customerId()).isEqualTo("guest");
        assertThat(response.currency()).isEqualTo("USD");
        assertThat(response.totalAmount()).isEqualByComparingTo("145.0000");
        assertThat(response.discountAmount()).isEqualByComparingTo("5.0000");
        assertThat(response.discountCode()).isEqualTo("FLAT5");
        assertThat(response.status()).isEqualTo(ReservationStatus.HOLD);
        assertThat(response.seats()).hasSize(2);
    }

    @Test
    void holdRejectsEventThatIsNotPublished() {
        when(eventClient.getEvent(eventId)).thenReturn(new EventDetails(eventId, venueId, "DRAFT", "{}"));

        assertThatThrownBy(() -> service.hold(request(null), "hold-2"))
                .isInstanceOf(EventNotBookableException.class);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void holdRejectsUnknownSeat() {
        when(eventClient.getEvent(eventId)).thenReturn(new EventDetails(eventId, venueId, "PUBLISHED",
                "{\"VIP\":100}"));
        when(seatClient.getSeats(any())).thenReturn(List.of(
                new SeatDetails(vipSeatId, venueId, "VIP")));

        assertThatThrownBy(() -> service.hold(request(null), "hold-3"))
                .isInstanceOf(EventNotBookableException.class);
    }

    @Test
    void holdIsIdempotentByIdempotencyKey() {
        ReservationEntity existing = new ReservationEntity(eventId, "guest", "guest@ticketing.local",
                new BigDecimal("145.0000"), "USD", Instant.now().plus(Duration.ofMinutes(5)));
        when(repository.findByHoldKey("hold-1")).thenReturn(Optional.of(existing));

        var response = service.hold(request("FLAT5"), "hold-1");

        assertThat(response.id()).isEqualTo(existing.getId());
        verify(eventClient, never()).getEvent(any());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void confirmRejectsExpiredHold() {
        UUID reservationId = UUID.randomUUID();
        ReservationEntity expired = new ReservationEntity(eventId, "guest", "guest@ticketing.local",
                new BigDecimal("100.0000"), "USD", Instant.now().minus(Duration.ofMinutes(1)));
        expired.setStatus(ReservationStatus.EXPIRED);
        when(repository.findById(reservationId)).thenReturn(Optional.of(expired));
        when(repository.transitionToConfirmed(reservationId, Instant.parse("2026-01-01T00:00:00Z"))).thenReturn(0);

        assertThatThrownBy(() -> service.confirm(reservationId))
                .isInstanceOf(ReservationExpiredException.class);
    }

    private HoldReservationRequest request(String discountCode) {
        return new HoldReservationRequest(eventId,
                List.of(vipSeatId, standardSeatId), discountCode, "USD", null);
    }
}