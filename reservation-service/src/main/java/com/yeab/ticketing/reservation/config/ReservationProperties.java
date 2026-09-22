package com.yeab.ticketing.reservation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@code app.reservation.*} configuration. Discount codes are configured rather
 * than stored in a database so they can be updated by operators without a
 * migration, matching the spec's "operator-configured codes" requirement.
 */
@ConfigurationProperties(prefix = "app.reservation")
public class ReservationProperties {

    private Duration holdDuration = Duration.ofMinutes(10);
    private Map<String, DiscountDefinition> discounts = new LinkedHashMap<>();

    public Duration getHoldDuration() {
        return holdDuration;
    }

    public void setHoldDuration(Duration holdDuration) {
        this.holdDuration = holdDuration;
    }

    public Map<String, DiscountDefinition> getDiscounts() {
        return discounts;
    }

    public void setDiscounts(Map<String, DiscountDefinition> discounts) {
        this.discounts = discounts;
    }

    public enum DiscountType {
        PERCENT, FIXED
    }

    public static class DiscountDefinition {
        private DiscountType type = DiscountType.PERCENT;
        private BigDecimal value = BigDecimal.ZERO;

        public DiscountType getType() {
            return type;
        }

        public void setType(DiscountType type) {
            this.type = type;
        }

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }
    }
}