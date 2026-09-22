package com.yeab.ticketing.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class TicketingClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}