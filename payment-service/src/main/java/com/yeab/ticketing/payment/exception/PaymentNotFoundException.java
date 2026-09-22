package com.yeab.ticketing.payment.exception;

import java.util.UUID;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(UUID id) { super("Payment not found: " + id); }
    public PaymentNotFoundException(String reference) { super("Payment not found for reference: " + reference); }
}
