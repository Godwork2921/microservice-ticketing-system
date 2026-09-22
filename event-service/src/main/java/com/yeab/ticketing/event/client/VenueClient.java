package com.yeab.ticketing.event.client;

import java.util.UUID;

public interface VenueClient {

    int seatCount(UUID venueId);
}