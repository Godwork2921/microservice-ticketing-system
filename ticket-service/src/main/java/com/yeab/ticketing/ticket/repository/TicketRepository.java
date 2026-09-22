package com.yeab.ticketing.ticket.repository;

import com.yeab.ticketing.ticket.entity.TicketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<TicketEntity, UUID> {
    List<TicketEntity> findByReservationId(UUID reservationId);
    Page<TicketEntity> findByCustomerIdOrderByIssuedAtDesc(String customerId, Pageable pageable);
    Optional<TicketEntity> findByTicketCode(String ticketCode);
}