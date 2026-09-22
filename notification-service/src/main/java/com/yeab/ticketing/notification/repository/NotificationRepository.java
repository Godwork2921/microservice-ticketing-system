package com.yeab.ticketing.notification.repository;

import com.yeab.ticketing.notification.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {
    Optional<NotificationEntity> findByIdempotencyKey(String idempotencyKey);
    Page<NotificationEntity> findByCustomerIdOrderByCreatedAtDesc(String customerId, Pageable pageable);
}
