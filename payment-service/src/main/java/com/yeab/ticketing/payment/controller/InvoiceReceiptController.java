package com.yeab.ticketing.payment.controller;

import com.yeab.ticketing.payment.dto.InvoiceResponse;
import com.yeab.ticketing.payment.dto.ReceiptResponse;
import com.yeab.ticketing.payment.service.InvoiceReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Invoices & Receipts", description = "Invoice and receipt management")
@RestController
@RequestMapping("/api/payments")
public class InvoiceReceiptController {

    private final InvoiceReceiptService service;

    public InvoiceReceiptController(InvoiceReceiptService service) {
        this.service = service;
    }

    // --- Invoice endpoints ---

    @Operation(summary = "Get invoice by ID")
    @GetMapping("/invoices/{invoiceId}")
    public InvoiceResponse getInvoiceById(@PathVariable UUID invoiceId) {
        return service.getInvoiceById(invoiceId);
    }

    @Operation(summary = "Get invoice by payment ID")
    @GetMapping("/{paymentId}/invoice")
    public InvoiceResponse getInvoiceByPaymentId(@PathVariable UUID paymentId) {
        return service.getInvoiceByPaymentId(paymentId);
    }

    @Operation(summary = "Get invoice by reservation ID")
    @GetMapping("/invoices/reservation/{reservationId}")
    public InvoiceResponse getInvoiceByReservationId(@PathVariable UUID reservationId) {
        return service.getInvoiceByReservationId(reservationId);
    }

    @Operation(summary = "List invoices for a customer")
    @GetMapping("/invoices")
    public Page<InvoiceResponse> listInvoices(
            @RequestParam String customerId,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.listInvoices(customerId, pageable);
    }

    // --- Receipt endpoints ---

    @Operation(summary = "Get receipt by ID")
    @GetMapping("/receipts/{receiptId}")
    public ReceiptResponse getReceiptById(@PathVariable UUID receiptId) {
        return service.getReceiptById(receiptId);
    }

    @Operation(summary = "Get receipt by payment ID")
    @GetMapping("/{paymentId}/receipt")
    public ReceiptResponse getReceiptByPaymentId(@PathVariable UUID paymentId) {
        return service.getReceiptByPaymentId(paymentId);
    }

    @Operation(summary = "Get receipt by reservation ID")
    @GetMapping("/receipts/reservation/{reservationId}")
    public ReceiptResponse getReceiptByReservationId(@PathVariable UUID reservationId) {
        return service.getReceiptByReservationId(reservationId);
    }

    @Operation(summary = "List receipts for a customer")
    @GetMapping("/receipts")
    public Page<ReceiptResponse> listReceipts(
            @RequestParam String customerId,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.listReceipts(customerId, pageable);
    }
}
