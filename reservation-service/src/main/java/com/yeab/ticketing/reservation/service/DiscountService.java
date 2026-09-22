package com.yeab.ticketing.reservation.service;

import com.yeab.ticketing.reservation.config.ReservationProperties;
import com.yeab.ticketing.reservation.exception.DiscountNotApplicableException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Applies configured discount codes via a small strategy map (PERCENT vs FIXED),
 * driven by {@code app.reservation.discounts}. Unknown codes are rejected.
 */
@Service
public class DiscountService {

    private final ReservationProperties properties;

    public DiscountService(ReservationProperties properties) {
        this.properties = properties;
    }

    /**
     * @return the discount amount (always {@code >= 0}); subtotal unchanged when
     * no code is supplied.
     */
    public BigDecimal apply(String code, BigDecimal subtotal) {
        if (code == null || code.isBlank()) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        ReservationProperties.DiscountDefinition def = properties.getDiscounts().get(code);
        if (def == null) {
            throw new DiscountNotApplicableException("Unknown discount code '" + code + "'");
        }
        BigDecimal discount;
        switch (def.getType()) {
            case PERCENT -> {
                if (def.getValue().compareTo(BigDecimal.valueOf(100)) > 0) {
                    throw new DiscountNotApplicableException("Discount code '" + code + "' is misconfigured (over 100%)");
                }
                discount = subtotal.multiply(def.getValue()).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            }
            case FIXED -> discount = def.getValue().setScale(4, RoundingMode.HALF_UP);
            default -> throw new DiscountNotApplicableException("Discount code '" + code + "' is misconfigured");
        }
        return discount.min(subtotal).max(BigDecimal.ZERO);
    }
}