package com.yeab.ticketing.payment;

import com.yeab.ticketing.payment.client.ReservationClient;
import com.yeab.ticketing.payment.client.ReservationDetails;
import com.yeab.ticketing.payment.enums.OutboxStatus;
import com.yeab.ticketing.payment.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Testcontainers
@Timeout(120)
class PaymentWebhookIntegrationTest {

    private static final String WEBHOOK_SECRET = "ticketing-dev-webhook-secret";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OutboxRepository outboxRepository;

    @MockitoBean
    private ReservationClient reservationClient;

    @Test
    void signedWebhookMarksPaymentSucceededAndOutboxPublishesToKafka() throws Exception {
        UUID reservationId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("525.0000");
        when(reservationClient.getReservation(reservationId))
                .thenReturn(new ReservationDetails(reservationId, "HOLD", amount, "ETB",
                        "customer-1", "customer-1@ticketing.local"));

        String createBody = """
                {
                  "reservationId": "%s",
                  "customerId": "customer-1",
                  "amount": 525.0000,
                  "currency": "ETB",
                  "provider": "mock",
                  "email": "customer-1@ticketing.local"
                }
                """.formatted(reservationId);

        String createResult = mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();
        UUID paymentId = UUID.fromString(objectMapper.readTree(createResult).path("id").asText());

        String webhookBody = """
                {
                  "txRef": "tx-%s",
                  "status": "SUCCESS",
                  "amount": 525.0000,
                  "currency": "ETB",
                  "reference": "provider-ref-1"
                }
                """.formatted(paymentId);

        mockMvc.perform(post("/api/payments/webhook/mock")
                        .header("X-Webhook-Signature", sign(webhookBody))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(webhookBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCEEDED"));

        mockMvc.perform(get("/api/payments/{paymentId}", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentId.toString()))
                .andExpect(jsonPath("$.status").value("SUCCEEDED"));

        awaitPublished(2);
    }

    private String sign(String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(WEBHOOK_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
    }

    private void awaitPublished(int expected) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 45_000;
        List<com.yeab.ticketing.payment.entity.OutboxEntity> rows = outboxRepository.findAll();
        while (System.currentTimeMillis() < deadline) {
            rows = outboxRepository.findAll();
            long published = rows.stream()
                    .filter(outbox -> outbox.getStatus() == OutboxStatus.PUBLISHED)
                    .count();
            if (published >= expected) {
                return;
            }
            Thread.sleep(500);
        }
        String dump = rows.stream()
                .map(outbox -> outbox.getEventType() + "=" + outbox.getStatus() + "(attempts:" + outbox.getAttemptCount() + ")")
                .collect(java.util.stream.Collectors.joining(", "));
        throw new org.opentest4j.AssertionFailedError(
                "expected at least " + expected + " PUBLISHED outbox rows, saw 0 [" + dump + "]");
    }
}