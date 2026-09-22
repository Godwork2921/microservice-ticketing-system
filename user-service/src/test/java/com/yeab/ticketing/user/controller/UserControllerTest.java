package com.yeab.ticketing.user.controller;

import com.yeab.ticketing.user.dto.request.RegisterUserRequest;
import com.yeab.ticketing.user.dto.response.UserResponse;
import com.yeab.ticketing.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = UserController.class, properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.security.oauth2.server.resource.autoconfigure.web.OAuth2ResourceServerWebSecurityAutoConfiguration,org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration"
})
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private UserService userService;

    @Test void getByIdReturnsUser() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.getById(id)).thenReturn(new UserResponse(id, "a@b.com", "Abebe", "Bekele",
                null, "en", Instant.parse("2030-01-01T00:00:00Z"), Instant.parse("2030-01-01T00:00:00Z")));
        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("a@b.com"))
                .andExpect(jsonPath("$.firstName").value("Abebe"));
    }

    @Test void createRegistersUser() throws Exception {
        UUID id = UUID.randomUUID();
        RegisterUserRequest request = new RegisterUserRequest(id, "new@example.com", "Rahel", null, null, null);
        when(userService.create(any(RegisterUserRequest.class))).thenReturn(new UserResponse(
                id, "new@example.com", "Rahel", null, null, null,
                Instant.parse("2030-01-01T00:00:00Z"), Instant.parse("2030-01-01T00:00:00Z")));
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"id": "%s", "email": "new@example.com", "firstName": "Rahel"}
                            """.formatted(id)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }
}