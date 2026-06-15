package com.synapse.knowledge.search.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.synapse.knowledge.search.config.SearchProperties;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class PlatformTenantClientTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void returnsTenantIdFromPrimaryBaseUrl() throws Exception {
        UUID tenantId = UUID.randomUUID();
        AtomicReference<String> authorization = new AtomicReference<>();
        String baseUrl = startTenantServer(tenantId, authorization);
        PlatformTenantClient client = new PlatformTenantClient(
            RestClient.builder().baseUrl(baseUrl).build(),
            RestClient.builder(),
            searchProperties(baseUrl),
            "http://unused-fallback"
        );

        UUID resolved = client.getMyTenantId("primary-token");

        assertThat(resolved).isEqualTo(tenantId);
        assertThat(authorization.get()).isEqualTo("Bearer primary-token");
    }

    @Test
    void retriesAgainstFallbackWhenLoopbackBaseUrlConnectionFails() throws Exception {
        UUID tenantId = UUID.randomUUID();
        AtomicReference<String> authorization = new AtomicReference<>();
        String fallbackBaseUrl = startTenantServer(tenantId, authorization);
        String unavailableLoopbackUrl = "http://localhost:" + findUnusedPort();
        PlatformTenantClient client = new PlatformTenantClient(
            RestClient.builder().baseUrl(unavailableLoopbackUrl).build(),
            RestClient.builder(),
            searchProperties(unavailableLoopbackUrl),
            fallbackBaseUrl
        );

        UUID resolved = client.getMyTenantId("fallback-token");

        assertThat(resolved).isEqualTo(tenantId);
        assertThat(authorization.get()).isEqualTo("Bearer fallback-token");
    }

    private SearchProperties searchProperties(String platformBaseUrl) {
        return new SearchProperties(
            new SearchProperties.Ai("http://localhost:8090", Duration.ofSeconds(3), 0.55d),
            new SearchProperties.Platform(platformBaseUrl, Duration.ofSeconds(1)),
            new SearchProperties.Hybrid(40, 5),
            new SearchProperties.Accuracy(
                "test-v1",
                910000L,
                "benchmark-search",
                "11111111-1111-1111-1111-111111111111",
                10,
                Duration.ofSeconds(5)
            ),
            new SearchProperties.Bm25(1.4d, 0.65d, 4.0d, 1.0d, 2.5d, "70%")
        );
    }

    private String startTenantServer(UUID tenantId, AtomicReference<String> authorization) throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/v1/tenants/me", exchange -> handleTenantRequest(exchange, tenantId, authorization));
        server.start();
        return "http://localhost:" + server.getAddress().getPort();
    }

    private void handleTenantRequest(HttpExchange exchange, UUID tenantId, AtomicReference<String> authorization) throws IOException {
        authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
        byte[] body = ("{\"id\":\"" + tenantId + "\"}").getBytes();
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(body);
        }
    }

    private int findUnusedPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
