package com.yeab.ticketing.reservation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Injectable clock so hold-expiration and CAS timestamps are deterministic in tests.
 */
@Configuration
public class TicketingClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}