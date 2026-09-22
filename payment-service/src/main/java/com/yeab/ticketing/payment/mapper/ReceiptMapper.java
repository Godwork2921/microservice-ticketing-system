package com.yeab.ticketing.payment.mapper;

import com.yeab.ticketing.payment.dto.ReceiptResponse;
import com.yeab.ticketing.payment.entity.ReceiptEntity;

public final class ReceiptMapper {

    private ReceiptMapper() { }

    public static ReceiptResponse toResponse(ReceiptEntity receipt) {
        return new ReceiptResponse(
                receipt.getId(),
                receipt.getPaymentId(),
                receipt.getInvoiceId(),
                receipt.getReservationId(),
                receipt.getCustomerId(),
                receipt.getReceiptNumber(),
                receipt.getAmountPaid(),
                receipt.getCurrency(),
                receipt.getProvider(),
                receipt.getProviderPaymentId(),
                receipt.getPaidAt(),
                receipt.getNotes(),
                receipt.getCreatedAt()
        );
    }
}
