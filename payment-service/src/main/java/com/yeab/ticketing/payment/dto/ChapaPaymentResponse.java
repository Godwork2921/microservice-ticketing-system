package com.yeab.ticketing.payment.dto;

import java.util.UUID;

public record ChapaPaymentResponse(UUID paymentId, String status, String checkoutUrl, String txRef) { }
