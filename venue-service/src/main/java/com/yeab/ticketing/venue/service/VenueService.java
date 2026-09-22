package com.yeab.ticketing.venue.service;

import com.yeab.ticketing.venue.dto.request.CreateVenueRequest;
import com.yeab.ticketing.venue.dto.request.UpdateVenueRequest;
import com.yeab.ticketing.venue.dto.response.VenueResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface VenueService {
    VenueResponse create(CreateVenueRequest request);
    VenueResponse get(UUID venueId);
    Page<VenueResponse> search(String query, Pageable pageable);
    VenueResponse update(UUID venueId, UpdateVenueRequest request);
}
