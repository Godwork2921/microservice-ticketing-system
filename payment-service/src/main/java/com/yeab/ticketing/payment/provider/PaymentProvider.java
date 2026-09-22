package com.yeab.ticketing.payment.provider;

import java.math.BigDecimal;

/**
 * Pluggable payment-provider strategy. The active provider is selected by
 * {@code app.payment.provider} (default {@code mock}); the strategy defines a
 * narrow contract so the rest of the service never depends on a vendor API.
 */
public interface PaymentProvider {

    String name();

    /**
     * Starts an external payment. Implementations must validate the required
     * customer details themselves and raise a provider-specific exception when
     * they are missing.
     */
    Reference initialize(Initiation initiation);

    /**
     * Queries the provider for the current state of a payment.
     */
    Verification verify(String providerId);

    /**
     * Handles an inbound provider webhook. Providers performing server-side
     * verification (e.g. Chapa) must not trust the webhook body's status.
     */
    Verification applyWebhook(String providerId, String claimedStatus, BigDecimal amount, String currency, String reference);

    record Initiation(BigDecimal amount, String currency, String customerEmail,
                      String firstName, String lastName, String phoneNumber,
                      String txRef, String callbackUrl, String returnUrl) { }

    record Reference(String providerId, String checkoutUrl) { }

    record Verification(boolean successful, boolean amountMatches, BigDecimal amount,
                        String currency, String reference, String message) { }
}