package com.yeab.ticketing.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChapaInitializeResponse(String message, String status, ChapaData data) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChapaData(@JsonProperty("checkout_url") String checkoutUrl,
                            @JsonProperty("tx_ref") String txRef) { }
}
