package com.yeab.ticketing.payment.repository;

import com.yeab.ticketing.payment.entity.ReceiptEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReceiptRepository extends JpaRepository<ReceiptEntity, UUID> {

    Optional<ReceiptEntity> findByPaymentId(UUID paymentId);

    Optional<ReceiptEntity> findByReservationId(UUID reservationId);

    Optional<ReceiptEntity> findByReceiptNumber(String receiptNumber);

    Page<ReceiptEntity> findByCustomerIdOrderByPaidAtDesc(String customerId, Pageable pageable);
}
