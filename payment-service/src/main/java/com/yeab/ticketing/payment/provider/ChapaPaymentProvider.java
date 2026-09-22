package com.yeab.ticketing.payment.provider;

import com.yeab.ticketing.payment.dto.ChapaInitializeResponse;
import com.yeab.ticketing.payment.dto.ChapaVerifyResponse;
import com.yeab.ticketing.payment.exception.ChapaIntegrationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * Real Chapa integration. Webhooks are re-verified server-side via the Chapa
 * transaction-verify API (using the secret key), so a forged webhook body is
 * never trusted; the claimed status is ignored.
 */
@Component
@ConditionalOnProperty(name = "app.payment.provider", havingValue = "chapa")
public class ChapaPaymentProvider implements PaymentProvider {

    public static final String NAME = "chapa";

    private final ChapaClient chapaClient;

    public ChapaPaymentProvider(ChapaClient chapaClient) {
        this.chapaClient = chapaClient;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Reference initialize(Initiation initiation) {
        required(initiation.customerEmail(), "email");
        required(initiation.firstName(), "firstName");
        required(initiation.lastName(), "lastName");
        required(initiation.phoneNumber(), "phoneNumber");
        ChapaInitializeResponse response = chapaClient.initialize(
                initiation.amount(), initiation.currency(), initiation.customerEmail(),
                initiation.firstName(), initiation.lastName(), initiation.phoneNumber(),
                initiation.txRef(), initiation.callbackUrl(), initiation.returnUrl());
        return new Reference(initiation.txRef(), response.data().checkoutUrl());
    }

    @Override
    public Verification verify(String providerId) {
        ChapaVerifyResponse response = chapaClient.verify(providerId);
        ChapaVerifyResponse.ChapaVerifyData data = response.data();
        boolean successful = "success".equalsIgnoreCase(response.status())
                && data != null
                && providerId.equals(data.txRef());
        BigDecimal amount = data == null || data.amount() == null
                ? null : new BigDecimal(data.amount());
        return new Verification(successful, amount != null, amount,
                data == null ? null : data.currency(),
                data == null ? null : data.reference(),
                successful ? null : response.message());
    }

    /**
     * Chapa's payload is not self-authenticating; always re-fetch from Chapa.
     */
    @Override
    public Verification applyWebhook(String providerId, String claimedStatus,
                                     BigDecimal amount, String currency, String reference) {
        return verify(providerId);
    }

    private void required(String value, String field) {
        if (!StringUtils.hasText(value)) {
            throw new ChapaIntegrationException(field + " is required for the Chapa provider");
        }
    }
}