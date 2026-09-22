package com.yeab.ticketing.notification.repository;

import com.yeab.ticketing.notification.entity.OutboxEntity;
import com.yeab.ticketing.notification.enums.OutboxStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")})
    @Query("select o from OutboxEntity o where o.status = :status order by o.createdAt asc")
    List<OutboxEntity> findPending(Pageable pageable, OutboxStatus status);
}