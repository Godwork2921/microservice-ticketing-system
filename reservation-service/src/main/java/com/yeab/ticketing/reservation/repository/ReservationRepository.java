package com.yeab.ticketing.reservation.repository;

import com.yeab.ticketing.reservation.entity.ReservationEntity;
import com.yeab.ticketing.reservation.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<ReservationEntity, UUID> {

    Page<ReservationEntity> findByCustomerIdOrderByCreatedAtDesc(String customerId, Pageable pageable);

    Optional<ReservationEntity> findByHoldKey(String holdKey);

    List<ReservationEntity> findByStatusAndHoldExpiresAtBefore(ReservationStatus status, Instant before);

    /**
     * Atomic compare-and-set: only a HOLD reservation whose hold has NOT expired
     * can become CONFIRMED. Returns 1 when the transition happened, 0 otherwise.
     * This is the primary guard against "expired hold confirmed" and double confirm.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ReservationEntity r
            set r.status = com.yeab.ticketing.reservation.enums.ReservationStatus.CONFIRMED,
                r.confirmedAt = :now
            where r.id = :id
              and r.status = com.yeab.ticketing.reservation.enums.ReservationStatus.HOLD
              and (r.holdExpiresAt is null or r.holdExpiresAt > :now)
            """)
    int transitionToConfirmed(@Param("id") UUID id, @Param("now") Instant now);

    /**
     * Atomic compare-and-set: HOLD or CONFIRMED reservations can be cancelled.
     * Returns 1 when the transition happened, 0 otherwise.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ReservationEntity r
            set r.status = com.yeab.ticketing.reservation.enums.ReservationStatus.CANCELLED
            where r.id = :id
              and r.status in (com.yeab.ticketing.reservation.enums.ReservationStatus.HOLD,
                               com.yeab.ticketing.reservation.enums.ReservationStatus.CONFIRMED)
            """)
    int transitionToCancelled(@Param("id") UUID id);

    /**
     * Atomic compare-and-set used by the expiry scheduler. Only HOLD reservations
     * past their expiry can become EXPIRED, so a concurrent confirm cannot be lost.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ReservationEntity r
            set r.status = com.yeab.ticketing.reservation.enums.ReservationStatus.EXPIRED
            where r.id = :id
              and r.status = com.yeab.ticketing.reservation.enums.ReservationStatus.HOLD
              and r.holdExpiresAt is not null
              and r.holdExpiresAt <= :now
            """)
    int transitionToExpired(@Param("id") UUID id, @Param("now") Instant now);
}