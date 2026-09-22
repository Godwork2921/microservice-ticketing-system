package com.yeab.ticketing.payment.controller;

import com.yeab.ticketing.payment.dto.CreatePaymentRequest;
import com.yeab.ticketing.payment.dto.FailPaymentRequest;
import com.yeab.ticketing.payment.dto.PaymentResponse;
import com.yeab.ticketing.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Payments", description = "Initiate, verify and webhook payments")
@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @Operation(summary = "Create a payment", description = "The amount is always derived from the reservation server-side.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse create(@Valid @RequestBody CreatePaymentRequest request) {
        return service.create(request);
    }

    @Operation(summary = "List payments")
    @GetMapping
    public Page<PaymentResponse> list(@RequestParam(required = false) String customerId,
                                      @PageableDefault(size = 20) Pageable pageable) {
        return service.list(customerId, pageable);
    }

    @Operation(summary = "Get payment by id")
    @GetMapping("/{paymentId}")
    public PaymentResponse getById(@PathVariable UUID paymentId) {
        return service.getById(paymentId);
    }

    @Operation(summary = "Get payment by reservation id")
    @GetMapping("/reservation/{reservationId}")
    public PaymentResponse getByReservationId(@PathVariable UUID reservationId) {
        return service.getByReservationId(reservationId);
    }

    @Operation(summary = "Mark a pending payment as succeeded (dev/test path)")
    @PostMapping("/{paymentId}/succeed")
    public PaymentResponse succeed(@PathVariable UUID paymentId,
                                   @RequestParam(required = false) String providerPaymentId) {
        return service.succeed(paymentId, providerPaymentId);
    }

    @Operation(summary = "Mark a pending payment as failed")
    @PostMapping("/{paymentId}/fail")
    public PaymentResponse fail(@PathVariable UUID paymentId, @Valid @RequestBody FailPaymentRequest request) {
        return service.fail(paymentId, request);
    }

    @Operation(summary = "Verify a payment with the active provider")
    @GetMapping("/verify/{providerId}")
    public PaymentResponse verify(@PathVariable String providerId) {
        return service.verify(providerId);
    }

    @Operation(summary = "Provider webhook", description = "Authenticated with the X-Webhook-Signature header; " +
            "the amount is re-verified server-side by the active provider.")
    @PostMapping("/webhook/{provider}")
    public ResponseEntity<PaymentResponse> webhook(@PathVariable String provider,
                                                   @Parameter(description = "Hex HMAC-SHA256 of the raw body using the webhook secret")
                                                   @RequestHeader(value = "X-Webhook-Signature", required = false) String signature,
                                                   @RequestBody String rawBody) {
        return ResponseEntity.ok(service.handleWebhook(provider, rawBody, signature));
    }

    @Operation(summary = "Chapa callback (fans out to provider verify)")
    @GetMapping({"/chapa/callback", "/callback/{provider}"})
    public PaymentResponse providerCallback(@PathVariable(required = false) String provider,
                                            @RequestParam("tx_ref") String txRef) {
        return service.verify(txRef);
    }

    @Operation(summary = "Chapa callback (POST variant)")
    @PostMapping("/chapa/callback")
    public PaymentResponse providerCallbackPost(@RequestParam("tx_ref") String txRef) {
        return service.verify(txRef);
    }
}