package com.yeab.ticketing.venue.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeab.ticketing.venue.dto.request.SeatRequest;
import com.yeab.ticketing.venue.dto.response.SeatResponse;
import com.yeab.ticketing.venue.entity.SeatEntity;
import com.yeab.ticketing.venue.entity.VenueEntity;
import com.yeab.ticketing.venue.exception.InvalidSeatAttributesException;
import com.yeab.ticketing.venue.exception.SeatNotFoundException;
import com.yeab.ticketing.venue.exception.VenueNotFoundException;
import com.yeab.ticketing.venue.repository.SeatRepository;
import com.yeab.ticketing.venue.repository.VenueRepository;
import com.yeab.ticketing.venue.service.SeatService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class SeatServiceImpl implements SeatService {

    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;
    private final ObjectMapper objectMapper;

    public SeatServiceImpl(VenueRepository venueRepository, SeatRepository seatRepository, ObjectMapper objectMapper) {
        this.venueRepository = venueRepository;
        this.seatRepository = seatRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<SeatResponse> importSeats(UUID venueId, List<SeatRequest> requests) {
        VenueEntity venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new VenueNotFoundException(venueId));
        if (requests == null || requests.isEmpty()) return List.of();
        if (seatRepository.countByVenueId(venueId) + requests.size() > venue.getCapacity()) {
            throw new IllegalArgumentException("Seat import exceeds venue capacity.");
        }
        Set<String> positions = new HashSet<>();
        List<SeatEntity> seats = requests.stream().map(request -> {
            String key = request.section().trim() + "|" + request.row().trim() + "|" + request.number();
            if (!positions.add(key)) throw new DataIntegrityViolationException("Duplicate seat position: " + key);
            return new SeatEntity(venueId, request.section().trim(), request.row().trim(), request.number(),
                    normalizeAttributes(request.attributes()));
        }).toList();
        return seatRepository.saveAll(seats).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SeatResponse get(UUID id) {
        return seatRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new SeatNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SeatResponse> list(UUID venueId, Pageable pageable) {
        if (!venueRepository.existsById(venueId)) throw new VenueNotFoundException(venueId);
        return seatRepository.findByVenueId(venueId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatResponse> findByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        if (ids.size() > 500) {
            throw new IllegalArgumentException("Batch seat lookup allows at most 500 ids");
        }
        return seatRepository.findByIdIn(ids.stream().distinct().toList())
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long count(UUID venueId) {
        if (!venueRepository.existsById(venueId)) throw new VenueNotFoundException(venueId);
        return seatRepository.countByVenueId(venueId);
    }

    private String normalizeAttributes(String attributes) {
        if (attributes == null || attributes.isBlank()) return "{}";
        try {
            JsonNode node = objectMapper.readTree(attributes);
            if (node == null || !node.isObject()) throw new InvalidSeatAttributesException();
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException exception) {
            throw new InvalidSeatAttributesException();
        }
    }

    private SeatResponse toResponse(SeatEntity seat) {
        return new SeatResponse(seat.getId(), seat.getVenueId(), seat.getSection(), seat.getSeatRow(),
            seat.getNumber(), seat.getAttributes(), seat.getCreatedAt());
    }
}
