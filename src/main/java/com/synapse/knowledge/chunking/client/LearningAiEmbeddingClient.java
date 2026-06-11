package com.synapse.knowledge.chunking.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class LearningAiEmbeddingClient {

    private static final String EMBEDDINGS_PATH = "/ai/embeddings";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public LearningAiEmbeddingClient(
        @Qualifier("learningAiEmbeddingRestClient") RestClient restClient,
        ObjectMapper objectMapper
    ) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    public EmbeddingBatchResponse createEmbeddings(String actorId, List<String> texts) {
        try {
            String responseBody = restClient.post()
                .uri(EMBEDDINGS_PATH)
                .header("X-User-Id", actorId)
                .body(new EmbeddingBatchRequest(texts))
                .retrieve()
                .body(String.class);

            if (responseBody == null || responseBody.isBlank()) {
                throw new IllegalStateException("learning-ai 임베딩 응답이 비어 있습니다");
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode payload = root.has("data") ? root.get("data") : root;
            EmbeddingBatchResponse response = objectMapper.treeToValue(payload, EmbeddingBatchResponse.class);
            if (response == null || response.embeddings() == null) {
                throw new IllegalStateException("learning-ai 임베딩 응답에 embeddings가 없습니다");
            }
            return response;
        } catch (RestClientException | IOException ex) {
            throw new IllegalStateException("learning-ai 임베딩 호출에 실패했습니다", ex);
        }
    }

    private record EmbeddingBatchRequest(List<String> texts) {
    }

    public record EmbeddingBatchResponse(
        List<List<Float>> embeddings,
        String model
    ) {
    }
}
