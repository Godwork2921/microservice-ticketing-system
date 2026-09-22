package com.yeab.ticketing.reservation.client;

import java.util.UUID;

/**
 * Lightweight view of an event owned by event-service. Fetched over REST; never
 * duplicated as a JPA entity in reservation-service.
 */
public record EventDetails(UUID id, UUID venueId, String status, String pricingRules) {
}