package com.yeab.ticketing.payment.exception;

/**
 * Invalid webhook signature (or missing webhook secret). Mapped to
 * 401 WEBHOOK_SIGNATURE_INVALID.
 */
public class WebhookVerificationException extends RuntimeException {
    public WebhookVerificationException(String message) {
        super(message);
    }
}