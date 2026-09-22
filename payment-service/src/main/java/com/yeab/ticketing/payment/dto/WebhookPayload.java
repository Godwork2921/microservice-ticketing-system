package com.yeab.ticketing.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * Payload of a signed webhook request. The {@code X-Webhook-Signature} header
 * must be a hex HMAC-SHA256 of this JSON body computed with the configured
 * webhook secret; providers additionally re-verify server-side.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WebhookPayload(String txRef, String status, BigDecimal amount,
                             String currency, String reference) { }