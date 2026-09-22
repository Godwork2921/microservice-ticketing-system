package com.yeab.ticketing.payment.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Deterministic provider for development, tests and CI. Charges are recorded
 * through webhook requests (signed with the shared webhook secret); until a
 * webhook arrives, {@code verify} reports the payment as not yet successful.
 * The in-memory ledger is intentionally instance-local: this provider is for
 * dev/test only, never for a multi-instance production Chapa deployment.
 */
@Component
@ConditionalOnProperty(name = "app.payment.provider", havingValue = "mock", matchIfMissing = true)
public class MockPaymentProvider implements PaymentProvider {

    public static final String NAME = "mock";

    private final Map<String, Charge> charges = new ConcurrentHashMap<>();

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Reference initialize(Initiation initiation) {
        return new Reference(initiation.txRef(), null);
    }

    @Override
    public Verification verify(String providerId) {
        Charge charge = charges.get(providerId);
        if (charge == null) {
            return new Verification(false, false, null, null, null,
                    "No charge recorded for " + providerId);
        }
        return new Verification(charge.successful, true, charge.amount, charge.currency,
                charge.reference, charge.successful ? null : "Payment failed");
    }

    @Override
    public Verification applyWebhook(String providerId, String claimedStatus,
                                     BigDecimal amount, String currency, String reference) {
        boolean successful = "SUCCESS".equalsIgnoreCase(claimedStatus)
                || "success".equalsIgnoreCase(claimedStatus)
                || "OK".equalsIgnoreCase(claimedStatus);
        charges.put(providerId, new Charge(successful, amount, currency, reference));
        return verify(providerId);
    }

    private record Charge(boolean successful, BigDecimal amount, String currency, String reference) { }
}