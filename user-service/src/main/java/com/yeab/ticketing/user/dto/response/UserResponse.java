package com.yeab.ticketing.user.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(UUID id, String email, String firstName, String lastName,
                           String phone, String locale, Instant createdAt, Instant updatedAt) { }