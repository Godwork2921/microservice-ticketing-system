package com.yeab.ticketing.venue.service.impl;

import com.yeab.ticketing.venue.dto.request.CreateVenueRequest;
import com.yeab.ticketing.venue.dto.request.UpdateVenueRequest;
import com.yeab.ticketing.venue.dto.response.VenueResponse;
import com.yeab.ticketing.venue.entity.VenueEntity;
import com.yeab.ticketing.venue.exception.VenueNotFoundException;
import com.yeab.ticketing.venue.repository.VenueRepository;
import com.yeab.ticketing.venue.repository.SeatRepository;
import com.yeab.ticketing.venue.service.VenueService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;

    public VenueServiceImpl(VenueRepository venueRepository, SeatRepository seatRepository) {
        this.venueRepository = venueRepository;
        this.seatRepository = seatRepository;
    }

    @Override
    public VenueResponse create(CreateVenueRequest request) {
        return toResponse(venueRepository.save(new VenueEntity(
                request.name().trim(), request.address().trim(), request.timezone().trim(), request.capacity())));
    }

    @Override
    @Transactional(readOnly = true)
    public VenueResponse get(UUID venueId) {
        return venueRepository.findById(venueId).map(this::toResponse)
                .orElseThrow(() -> new VenueNotFoundException(venueId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VenueResponse> search(String query, Pageable pageable) {
        String normalized = query == null ? null : query.trim().toLowerCase(Locale.ROOT);
        Specification<VenueEntity> specification = (root, ignored, builder) -> {
            if (normalized == null || normalized.isBlank()) return builder.conjunction();
            return builder.like(builder.lower(root.get("name")), "%" + normalized + "%");
        };
        return venueRepository.findAll(specification, pageable).map(this::toResponse);
    }

    @Override
    public VenueResponse update(UUID venueId, UpdateVenueRequest request) {
        VenueEntity venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new VenueNotFoundException(venueId));
        long seats = seatRepository.countByVenueId(venueId);
        if (request.capacity() < seats) {
            throw new IllegalArgumentException("capacity cannot be lower than the number of existing seats.");
        }
        venue.setName(request.name().trim());
        venue.setAddress(request.address().trim());
        venue.setTimezone(request.timezone().trim());
        venue.setCapacity(request.capacity());
        return toResponse(venueRepository.save(venue));
    }

    private VenueResponse toResponse(VenueEntity venue) {
        return new VenueResponse(venue.getId(), venue.getName(), venue.getAddress(), venue.getTimezone(),
                venue.getCapacity(), venue.getCreatedAt(), venue.getUpdatedAt());
    }
}
