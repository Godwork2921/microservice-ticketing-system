package com.yeab.ticketing.ticket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeab.ticketing.common.events.DomainEventEnvelope;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:ticket-service}")
    private String groupId;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DomainEventEnvelope>
            kafkaListenerContainerFactory(ObjectMapper objectMapper) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<DomainEventEnvelope> valueDeserializer =
                new JsonDeserializer<>(DomainEventEnvelope.class, objectMapper, false);

        DefaultKafkaConsumerFactory<String, DomainEventEnvelope> consumerFactory =
                new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);

        ConcurrentKafkaListenerContainerFactory<String, DomainEventEnvelope> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(1);
        return factory;
    }
}