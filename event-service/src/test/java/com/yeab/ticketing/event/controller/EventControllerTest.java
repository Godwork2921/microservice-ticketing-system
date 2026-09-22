package com.yeab.ticketing.event.controller;

import com.yeab.ticketing.event.dto.response.EventResponse;
import com.yeab.ticketing.event.enums.EventStatus;
import com.yeab.ticketing.event.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = EventController.class, properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.security.oauth2.server.resource.autoconfigure.web.OAuth2ResourceServerWebSecurityAutoConfiguration,org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration"
})
@AutoConfigureMockMvc(addFilters = false)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @Test
    void getByIdReturnsAnEventResponse() throws Exception {
        UUID eventId = UUID.randomUUID();
        EventResponse response = new EventResponse(
                eventId,
                UUID.randomUUID(),
                "Concert",
                Instant.parse("2030-06-01T18:00:00Z"),
                Instant.parse("2030-06-01T20:00:00Z"),
                EventStatus.PUBLISHED,
                "{}",
                150,
                Instant.parse("2030-01-01T00:00:00Z"),
                Instant.parse("2030-01-01T00:00:00Z")
        );
        when(eventService.getById(eventId)).thenReturn(response);

        mockMvc.perform(get("/api/events/{eventId}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId.toString()))
                .andExpect(jsonPath("$.title").value("Concert"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.seatCount").value(150));
    }
}
