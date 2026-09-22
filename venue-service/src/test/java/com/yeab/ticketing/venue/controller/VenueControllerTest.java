package com.yeab.ticketing.venue.controller;

import com.yeab.ticketing.venue.dto.response.SeatResponse;
import com.yeab.ticketing.venue.dto.response.VenueResponse;
import com.yeab.ticketing.venue.service.SeatService;
import com.yeab.ticketing.venue.service.VenueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = VenueController.class, properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.security.oauth2.server.resource.autoconfigure.web.OAuth2ResourceServerWebSecurityAutoConfiguration,org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration"
})
@AutoConfigureMockMvc(addFilters = false)
class VenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VenueService venueService;

    @MockitoBean
    private SeatService seatService;

    @Test
    void getVenueReturnsVenueResponse() throws Exception {
        UUID venueId = UUID.randomUUID();
        when(venueService.get(venueId)).thenReturn(new VenueResponse(
                venueId, "Arena", "Address", "UTC", 100,
                Instant.parse("2030-01-01T00:00:00Z"), Instant.parse("2030-01-01T00:00:00Z")));

        mockMvc.perform(get("/api/venues/{venueId}", venueId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(venueId.toString()))
                .andExpect(jsonPath("$.name").value("Arena"))
                .andExpect(jsonPath("$.capacity").value(100));
    }

    @Test
    void seatsByIdsReturnsSeats() throws Exception {
        UUID seatId = UUID.randomUUID();
        UUID venueId = UUID.randomUUID();
        when(seatService.findByIds(anyList())).thenReturn(List.of(new SeatResponse(
                seatId, venueId, "GA", "A", 1, "{}", Instant.parse("2030-01-01T00:00:00Z"))));

        mockMvc.perform(get("/api/venues/seats").param("ids", seatId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(seatId.toString()))
                .andExpect(jsonPath("$[0].section").value("GA"));
    }
}
