package com.yeab.ticketing.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeab.ticketing.common.json.JsonMapperFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Boot 4 auto-configures Jackson 3 ({@code tools.jackson}) while the services
 * are written against Jackson 2 ({@code com.fasterxml.jackson}). This
 * auto-configuration restores the Jackson 2 {@link ObjectMapper} bean the
 * platform's Kafka outboxes, pricing rules, and error handling rely on.
 */
@AutoConfiguration
@ConditionalOnClass(ObjectMapper.class)
public class CommonJacksonAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper() {
        return JsonMapperFactory.create();
    }
}