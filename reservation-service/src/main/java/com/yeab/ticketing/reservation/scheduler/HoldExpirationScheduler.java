package com.yeab.ticketing.reservation.scheduler;

import com.yeab.ticketing.common.events.EventTypes;
import com.yeab.ticketing.reservation.entity.ReservationEntity;
import com.yeab.ticketing.reservation.enums.ReservationStatus;
import com.yeab.ticketing.reservation.messaging.ReservationEventOutbox;
import com.yeab.ticketing.reservation.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Periodically expires HOLD reservations whose hold has elapsed, releasing their
 * seats (the released-flag flip makes them available for new reservations via
 * the partial unique index). Each expiry uses an atomic CAS so a concurrent
 * confirm is never silently lost.
 */
@Component
public class HoldExpirationScheduler {

    private static final Logger log = LoggerFactory.getLogger(HoldExpirationScheduler.class);

    private final ReservationRepository reservationRepository;
    private final ReservationEventOutbox eventOutbox;
    private final Clock clock;

    public HoldExpirationScheduler(ReservationRepository reservationRepository,
                                   ReservationEventOutbox eventOutbox,
                                   Clock clock) {
        this.reservationRepository = reservationRepository;
        this.eventOutbox = eventOutbox;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${app.reservation.expiry-check-interval-ms:15000}", initialDelayString = "${app.reservation.expiry-check-interval-ms:15000}")
    @Transactional
    public void expireHolds() {
        Instant now = clock.instant();
        List<ReservationEntity> expiring = reservationRepository
                .findByStatusAndHoldExpiresAtBefore(ReservationStatus.HOLD, now);
        for (ReservationEntity reservation : expiring) {
            int updated = reservationRepository.transitionToExpired(reservation.getId(), now);
            if (updated == 1) {
                reservation.getSeats().forEach(seat -> seat.setReleased(true));
                eventOutbox.recordReservationStateChanged(
                        EventTypes.RESERVATION_EXPIRED, reservation.getId(), reservation.getEventId(),
                        reservation.getCustomerId(), reservation.getCustomerEmail(), reservation.getCurrency(),
                        reservation.getTotalAmount(), reservation.getSeats().size());
                log.info("Expired hold on reservation {} (held seats released)", reservation.getId());
            }
        }
    }
}