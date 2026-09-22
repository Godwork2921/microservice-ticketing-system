package com.yeab.ticketing.payment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ChapaConfig {
    @Bean
    RestClient chapaRestClient(@Value("${app.chapa.base-url:https://api.chapa.co}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
