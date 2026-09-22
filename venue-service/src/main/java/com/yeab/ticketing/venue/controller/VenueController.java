package com.yeab.ticketing.venue.controller;

import com.yeab.ticketing.venue.dto.request.CreateVenueRequest;
import com.yeab.ticketing.venue.dto.request.SeatRequest;
import com.yeab.ticketing.venue.dto.request.UpdateVenueRequest;
import com.yeab.ticketing.venue.dto.response.PageResponse;
import com.yeab.ticketing.venue.dto.response.SeatResponse;
import com.yeab.ticketing.venue.dto.response.VenueResponse;
import com.yeab.ticketing.venue.service.SeatService;
import com.yeab.ticketing.venue.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;
    private final SeatService seatService;

    public VenueController(VenueService venueService, SeatService seatService) {
        this.venueService = venueService;
        this.seatService = seatService;
    }

    @GetMapping
    public PageResponse<VenueResponse> search(@RequestParam(required = false) String q,
                                               @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return PageResponse.from(venueService.search(q, pageable));
    }

    @GetMapping("/seats")
    public List<SeatResponse> seatsByIds(@RequestParam List<UUID> ids) {
        return seatService.findByIds(ids);
    }

    @GetMapping("/{venueId}")
    public VenueResponse get(@PathVariable UUID venueId) {
        return venueService.get(venueId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VenueResponse create(@Valid @RequestBody CreateVenueRequest request) {
        return venueService.create(request);
    }

    @PutMapping("/{venueId}")
    public VenueResponse update(@PathVariable UUID venueId, @Valid @RequestBody UpdateVenueRequest request) {
        return venueService.update(venueId, request);
    }

    @PostMapping("/{venueId}/seats")
    @ResponseStatus(HttpStatus.CREATED)
    public List<SeatResponse> importSeats(@PathVariable UUID venueId,
                                          @RequestBody List<@Valid SeatRequest> requests) {
        return seatService.importSeats(venueId, requests);
    }

    @GetMapping("/{venueId}/seats")
    public PageResponse<SeatResponse> listSeats(@PathVariable UUID venueId,
                                                 @PageableDefault(size = 100, sort = {"section", "seatRow", "number"}, direction = Sort.Direction.ASC) Pageable pageable) {
        return PageResponse.from(seatService.list(venueId, pageable));
    }

    @GetMapping("/{venueId}/seats/count")
    public com.yeab.ticketing.venue.dto.response.SeatCountResponse seatCount(@PathVariable UUID venueId) {
        return new com.yeab.ticketing.venue.dto.response.SeatCountResponse(seatService.count(venueId));
    }
}
