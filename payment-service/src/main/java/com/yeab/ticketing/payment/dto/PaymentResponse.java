package com.yeab.ticketing.payment.dto;

import com.yeab.ticketing.payment.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(UUID id, UUID reservationId, String customerId, BigDecimal amount,
                              String currency, PaymentStatus status, String provider,
                              String providerPaymentId, String checkoutUrl, String failureReason, Instant createdAt,
                              Instant updatedAt, Instant completedAt) { }
