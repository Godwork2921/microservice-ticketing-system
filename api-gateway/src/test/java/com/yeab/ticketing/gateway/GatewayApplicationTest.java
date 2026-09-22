package com.yeab.ticketing.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayApplicationTest {
    @LocalServerPort private int port;

    @Test void contextLoadsAndUnmatchedPathsReturn404() {
        WebTestClient client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
        client.get().uri("/api/unknown")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }
}