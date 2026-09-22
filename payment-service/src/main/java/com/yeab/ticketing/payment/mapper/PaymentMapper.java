package com.yeab.ticketing.payment.mapper;

import com.yeab.ticketing.payment.dto.PaymentResponse;
import com.yeab.ticketing.payment.entity.PaymentEntity;

public final class PaymentMapper {
    private PaymentMapper() { }
    public static PaymentResponse toResponse(PaymentEntity payment) {
        return new PaymentResponse(payment.getId(), payment.getReservationId(), payment.getCustomerId(),
                payment.getAmount(), payment.getCurrency(), payment.getStatus(), payment.getProvider(),
                payment.getProviderPaymentId(), payment.getCheckoutUrl(), payment.getFailureReason(), payment.getCreatedAt(),
                payment.getUpdatedAt(), payment.getCompletedAt());
    }
}
