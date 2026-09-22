package com.yeab.ticketing.venue.controller;

import com.yeab.ticketing.venue.dto.response.SeatResponse;
import com.yeab.ticketing.venue.service.SeatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/seats")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping("/{seatId}")
    public SeatResponse get(@PathVariable UUID seatId) {
        return seatService.get(seatId);
    }
}