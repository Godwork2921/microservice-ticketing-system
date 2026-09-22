package com.yeab.ticketing.venue.repository;

import com.yeab.ticketing.venue.entity.VenueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface VenueRepository extends JpaRepository<VenueEntity, UUID>, JpaSpecificationExecutor<VenueEntity> {
}
