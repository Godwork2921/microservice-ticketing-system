package com.yeab.ticketing.payment.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReceiptResponse(
        UUID id,
        UUID paymentId,
        UUID invoiceId,
        UUID reservationId,
        String customerId,
        String receiptNumber,
        BigDecimal amountPaid,
        String currency,
        String provider,
        String providerPaymentId,
        Instant paidAt,
        String notes,
        Instant createdAt
) { }
