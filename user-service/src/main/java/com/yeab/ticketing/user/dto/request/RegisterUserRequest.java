package com.yeab.ticketing.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RegisterUserRequest(UUID id,
                                  @NotBlank @Email @Size(max = 320) String email,
                                  @NotBlank @Size(max = 100) String firstName,
                                  @Size(max = 100) String lastName,
                                  @Size(max = 40) String phone,
                                  @Size(max = 10) String locale) { }