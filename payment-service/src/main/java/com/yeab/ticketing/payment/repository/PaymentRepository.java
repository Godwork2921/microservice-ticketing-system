package com.yeab.ticketing.payment.repository;

import com.yeab.ticketing.payment.entity.PaymentEntity;
import com.yeab.ticketing.payment.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {

    Optional<PaymentEntity> findByReservationId(UUID reservationId);

    Optional<PaymentEntity> findByProviderPaymentId(String providerPaymentId);

    Page<PaymentEntity> findByCustomerIdOrderByCreatedAtDesc(String customerId, Pageable pageable);

    /**
     * Atomic compare-and-set: only a PENDING payment can become SUCCEEDED, so a
     * webhook racing with a manual succeed cannot double-transition.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update PaymentEntity p
            set p.status = com.yeab.ticketing.payment.enums.PaymentStatus.SUCCEEDED,
                p.providerPaymentId = :providerPaymentId,
                p.completedAt = :now
            where p.id = :id and p.status = com.yeab.ticketing.payment.enums.PaymentStatus.PENDING
            """)
    int transitionToSucceeded(@Param("id") UUID id, @Param("providerPaymentId") String providerPaymentId,
                              @Param("now") Instant now);

    /**
     * Atomic compare-and-set: only a PENDING payment can become FAILED.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update PaymentEntity p
            set p.status = com.yeab.ticketing.payment.enums.PaymentStatus.FAILED,
                p.failureReason = :reason,
                p.completedAt = :now
            where p.id = :id and p.status = com.yeab.ticketing.payment.enums.PaymentStatus.PENDING
            """)
    int transitionToFailed(@Param("id") UUID id, @Param("reason") String reason, @Param("now") Instant now);
}