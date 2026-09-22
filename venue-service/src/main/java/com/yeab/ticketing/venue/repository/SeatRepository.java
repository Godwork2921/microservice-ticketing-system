package com.yeab.ticketing.venue.repository;

import com.yeab.ticketing.venue.entity.SeatEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<SeatEntity, UUID> {
    Page<SeatEntity> findByVenueId(UUID venueId, Pageable pageable);
    long countByVenueId(UUID venueId);
    List<SeatEntity> findByIdIn(Collection<UUID> ids);
}
