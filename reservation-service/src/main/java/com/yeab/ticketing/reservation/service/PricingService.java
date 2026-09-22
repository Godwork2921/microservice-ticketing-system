package com.yeab.ticketing.reservation.service;

import com.yeab.ticketing.reservation.exception.EventNotBookableException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Map;

@Service
public class PricingService {

    private static final String DEFAULT_SECTION = "DEFAULT";
    private final ObjectMapper objectMapper;

    public PricingService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Resolves the server-side price for a seat section from the event's
     * pricing rules map: {@code {"VIP":150,"STANDARD":75,"DEFAULT":50}}.
     * Falls back to DEFAULT; throws when the section has no price.
     */
    public BigDecimal priceFor(String section, String pricingRules) {
        if (section == null) {
            throw new EventNotBookableException("Seat has no section, cannot be priced");
        }
        Map<String, BigDecimal> rules = parseRules(pricingRules);
        BigDecimal price = rules.get(section);
        if (price == null) {
            price = rules.get(DEFAULT_SECTION);
        }
        if (price == null) {
            throw new EventNotBookableException("No price configured for section '" + section + "'");
        }
        if (price.signum() < 0) {
            throw new EventNotBookableException("Price cannot be negative for section '" + section + "'");
        }
        return price.setScale(4, RoundingMode.HALF_UP);
    }

    private Map<String, BigDecimal> parseRules(String pricingRules) {
        if (pricingRules == null || pricingRules.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(pricingRules, new TypeReference<Map<String, BigDecimal>>() { });
        } catch (Exception ex) {
            throw new EventNotBookableException("Event has malformed pricing rules");
        }
    }
}