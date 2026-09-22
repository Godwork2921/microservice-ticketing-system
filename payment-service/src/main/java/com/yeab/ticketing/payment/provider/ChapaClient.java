package com.yeab.ticketing.payment.provider;

import com.yeab.ticketing.payment.dto.ChapaInitializeResponse;
import com.yeab.ticketing.payment.dto.ChapaVerifyResponse;
import com.yeab.ticketing.payment.exception.ChapaIntegrationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ChapaClient {
    private final RestClient client;
    private final String secretKey;
    private final String defaultCallbackUrl;
    private final String defaultReturnUrl;

    public ChapaClient(RestClient chapaRestClient,
                       @Value("${app.chapa.secret-key:}") String secretKey,
                       @Value("${app.chapa.callback-url:}") String defaultCallbackUrl,
                       @Value("${app.chapa.return-url:}") String defaultReturnUrl) {
        this.client = chapaRestClient;
        this.secretKey = secretKey;
        this.defaultCallbackUrl = defaultCallbackUrl;
        this.defaultReturnUrl = defaultReturnUrl;
    }

    public ChapaInitializeResponse initialize(BigDecimal amount, String currency, String email,
                                              String firstName, String lastName, String phoneNumber,
                                              String txRef, String callbackUrl, String returnUrl) {
        requireSecret();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("amount", amount.toPlainString());
        body.put("currency", currency);
        body.put("email", email);
        body.put("first_name", firstName);
        body.put("last_name", lastName);
        body.put("phone_number", phoneNumber);
        body.put("tx_ref", txRef);
        body.put("callback_url", callbackUrl == null || callbackUrl.isBlank() ? defaultCallbackUrl : callbackUrl);
        body.put("return_url", returnUrl == null || returnUrl.isBlank() ? defaultReturnUrl : returnUrl);
        body.put("customization", Map.of("title", "Ticket payment", "description", "Event ticket reservation"));
        try {
            ChapaInitializeResponse response = client.post()
                    .uri("/v1/transaction/initialize")
                    .header("Authorization", "Bearer " + secretKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(ChapaInitializeResponse.class);
            if (response == null || response.data() == null || response.data().checkoutUrl() == null) {
                throw new ChapaIntegrationException("Chapa did not return a checkout URL");
            }
            return response;
        } catch (RestClientException exception) {
            throw new ChapaIntegrationException("Chapa initialization failed: " + exception.getMessage());
        }
    }

    public ChapaVerifyResponse verify(String txRef) {
        requireSecret();
        try {
            ChapaVerifyResponse response = client.get()
                    .uri("/v1/transaction/verify/{txRef}", txRef)
                    .header("Authorization", "Bearer " + secretKey)
                    .retrieve()
                    .body(ChapaVerifyResponse.class);
            if (response == null || response.data() == null) {
                throw new ChapaIntegrationException("Chapa returned an empty verification response");
            }
            return response;
        } catch (RestClientException exception) {
            throw new ChapaIntegrationException("Chapa verification failed: " + exception.getMessage());
        }
    }

    private void requireSecret() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new ChapaIntegrationException("CHAPA_SECRET_KEY is not configured");
        }
    }
}
