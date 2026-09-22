package com.yeab.ticketing.reservation.repository;

import com.yeab.ticketing.reservation.entity.ReservationSeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReservationSeatRepository extends JpaRepository<ReservationSeatEntity, UUID> { }
