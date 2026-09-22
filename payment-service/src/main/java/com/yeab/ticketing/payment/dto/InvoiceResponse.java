package com.yeab.ticketing.payment.dto;

import com.yeab.ticketing.payment.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        UUID paymentId,
        UUID reservationId,
        String customerId,
        String invoiceNumber,
        BigDecimal amount,
        String currency,
        InvoiceStatus status,
        Instant issuedAt,
        Instant dueAt,
        String notes,
        Instant createdAt
) { }
