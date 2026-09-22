package com.yeab.ticketing.reservation.exception;

/**
 * Invalid or inapplicable discount code. Mapped to 422 VALIDATION_FAILURE.
 */
public class DiscountNotApplicableException extends RuntimeException {
    public DiscountNotApplicableException(String message) {
        super(message);
    }
}