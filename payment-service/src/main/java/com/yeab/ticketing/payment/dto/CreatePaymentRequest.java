package com.yeab.ticketing.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentRequest(
        @NotNull UUID reservationId,
        @NotBlank String customerId,
        @NotNull @DecimalMin(value = "0.00") BigDecimal amount,
        @NotBlank @Pattern(regexp = "[A-Za-z]{3}") String currency,
        @NotBlank String provider,
        @Email String email,
        String firstName,
        String lastName,
        String phoneNumber,
        String returnUrl,
        String callbackUrl
) { }
