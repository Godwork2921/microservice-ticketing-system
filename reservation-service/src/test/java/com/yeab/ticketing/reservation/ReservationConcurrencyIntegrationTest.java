package com.yeab.ticketing.reservation;

import com.yeab.ticketing.reservation.client.EventClient;
import com.yeab.ticketing.reservation.client.EventDetails;
import com.yeab.ticketing.reservation.client.SeatClient;
import com.yeab.ticketing.reservation.client.SeatDetails;
import com.yeab.ticketing.reservation.dto.HoldReservationRequest;
import com.yeab.ticketing.reservation.exception.SeatAlreadyReservedException;
import com.yeab.ticketing.reservation.repository.ReservationRepository;
import com.yeab.ticketing.reservation.service.ReservationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
@Timeout(120)
class ReservationConcurrencyIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @MockitoBean
    private EventClient eventClient;

    @MockitoBean
    private SeatClient seatClient;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void parallelHoldsForTheSameSeatAllowOnlyOneWinner() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID venueId = UUID.randomUUID();
        UUID seatId = UUID.randomUUID();
        List<UUID> seats = List.of(seatId);

        when(eventClient.getEvent(eventId))
                .thenReturn(new EventDetails(eventId, venueId, "PUBLISHED", "{\"VIP\":100}"));
        when(seatClient.getSeats(seats))
                .thenReturn(List.of(new SeatDetails(seatId, venueId, "VIP")));

        int racers = 4;
        CountDownLatch ready = new CountDownLatch(racers);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(racers);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger conflicts = new AtomicInteger();
        AtomicReference<Throwable> unexpected = new AtomicReference<>();

        for (int i = 0; i < racers; i++) {
            String customerId = "customer-" + i;
            pool.submit(() -> {
                setSecurityContext(customerId);
                ready.countDown();
                try {
                    start.await();
                    reservationService.hold(new HoldReservationRequest(eventId, seats, null, "ETB", null), null);
                    successes.incrementAndGet();
                } catch (SeatAlreadyReservedException ex) {
                    conflicts.incrementAndGet();
                } catch (Throwable t) {
                    unexpected.set(t);
                }
            });
        }

        assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
        start.countDown();
        pool.shutdown();
        assertThat(pool.awaitTermination(30, TimeUnit.SECONDS)).isTrue();

        assertThat(unexpected.get()).isNull();
        assertThat(successes.get()).isEqualTo(1);
        assertThat(conflicts.get()).isEqualTo(racers - 1);
        assertThat(reservationRepository.findAll()).hasSize(1);

        SecurityContextHolder.clearContext();
        setSecurityContext("later");
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                reservationService.hold(new HoldReservationRequest(eventId, seats, null, "ETB", null), null))
                .isInstanceOf(SeatAlreadyReservedException.class);
    }

    private void setSecurityContext(String principal) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, List.of());
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }
}