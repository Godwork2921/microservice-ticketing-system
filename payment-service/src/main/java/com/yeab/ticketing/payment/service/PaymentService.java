package com.yeab.ticketing.payment.service;

import com.yeab.ticketing.payment.dto.CreatePaymentRequest;
import com.yeab.ticketing.payment.dto.FailPaymentRequest;
import com.yeab.ticketing.payment.dto.PaymentResponse;
import com.yeab.ticketing.payment.dto.WebhookPayload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PaymentService {
    PaymentResponse create(CreatePaymentRequest request);
    PaymentResponse getById(UUID paymentId);
    PaymentResponse getByReservationId(UUID reservationId);
    Page<PaymentResponse> list(String customerId, Pageable pageable);
    PaymentResponse succeed(UUID paymentId, String providerPaymentId);
    PaymentResponse fail(UUID paymentId, FailPaymentRequest request);
    PaymentResponse verify(String providerId);
    PaymentResponse handleWebhook(String provider, String rawBody, String signature);
}