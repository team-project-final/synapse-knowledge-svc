package com.synapse.knowledge.chunking.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class LearningAiEmbeddingClientTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @DisplayName("createEmbeddings_octetStream응답이어도_json으로파싱한다")
    @Test
    void createEmbeddings_octetStream응답이어도_json으로파싱한다() throws IOException {
        // Given
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/ai/embeddings", this::writeOctetStreamJsonResponse);
        server.start();

        LearningAiEmbeddingClient client = new LearningAiEmbeddingClient(
            RestClient.builder()
                .baseUrl("http://localhost:" + server.getAddress().getPort())
                .build(),
            new ObjectMapper()
        );

        // When
        LearningAiEmbeddingClient.EmbeddingBatchResponse response = client.createEmbeddings(
            "subject-123",
            List.of("첫 번째 청크")
        );

        // Then
        assertThat(response.model()).isEqualTo("text-embedding-3-small");
        assertThat(response.embeddings()).containsExactly(List.of(0.1f, 0.2f, 0.3f));
    }

    private void writeOctetStreamJsonResponse(HttpExchange exchange) throws IOException {
        byte[] body = """
            {"success":true,"data":{"embeddings":[[0.1,0.2,0.3]],"model":"text-embedding-3-small"}}
            """
            .getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
        exchange.sendResponseHeaders(200, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }
}
