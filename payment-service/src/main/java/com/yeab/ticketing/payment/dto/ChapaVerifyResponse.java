package com.yeab.ticketing.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChapaVerifyResponse(String message, String status, ChapaVerifyData data) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChapaVerifyData(String status,
                                  @JsonProperty("tx_ref") String txRef,
                                  String currency,
                                  String amount,
                                  @JsonProperty("reference") String reference) { }
}
