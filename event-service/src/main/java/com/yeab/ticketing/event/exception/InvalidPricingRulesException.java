package com.yeab.ticketing.event.exception;

public class InvalidPricingRulesException extends RuntimeException {

    public InvalidPricingRulesException() {
        super("pricingRules must be a valid JSON object.");
    }
}
