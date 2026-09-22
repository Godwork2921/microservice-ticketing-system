package com.yeab.ticketing.payment.mapper;

import com.yeab.ticketing.payment.dto.InvoiceResponse;
import com.yeab.ticketing.payment.entity.InvoiceEntity;

public final class InvoiceMapper {

    private InvoiceMapper() { }

    public static InvoiceResponse toResponse(InvoiceEntity invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getPaymentId(),
                invoice.getReservationId(),
                invoice.getCustomerId(),
                invoice.getInvoiceNumber(),
                invoice.getAmount(),
                invoice.getCurrency(),
                invoice.getStatus(),
                invoice.getIssuedAt(),
                invoice.getDueAt(),
                invoice.getNotes(),
                invoice.getCreatedAt()
        );
    }
}
