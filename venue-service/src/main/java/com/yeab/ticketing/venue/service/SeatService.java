package com.yeab.ticketing.venue.service;

import com.yeab.ticketing.venue.dto.request.SeatRequest;
import com.yeab.ticketing.venue.dto.response.SeatResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface SeatService {
    List<SeatResponse> importSeats(UUID venueId, List<SeatRequest> requests);
    SeatResponse get(UUID id);
    Page<SeatResponse> list(UUID venueId, Pageable pageable);
    List<SeatResponse> findByIds(List<UUID> ids);
    long count(UUID venueId);
}
