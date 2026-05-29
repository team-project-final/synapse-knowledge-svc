package com.synapse.knowledge.search.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.knowledge.search.config.SearchProperties;
import com.synapse.knowledge.search.dto.client.LearningAiSemanticRequest;
import com.synapse.knowledge.search.dto.client.LearningAiSemanticResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class LearningAiSearchClient {

    private static final String SEMANTIC_SEARCH_PATH = "/ai/search/semantic";

    @Qualifier("learningAiRestClient")
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final SearchProperties searchProperties;

    public List<LearningAiSemanticHit> searchSemantic(String semanticActorId, String query, int topK) {
        try {
            String responseBody = restClient.post()
                .uri(SEMANTIC_SEARCH_PATH)
                .header("X-User-Id", semanticActorId)
                .body(new LearningAiSemanticRequest(query, topK, searchProperties.ai().threshold()))
                .retrieve()
                .body(String.class);

            if (responseBody == null || responseBody.isBlank()) {
                return List.of();
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode payload = root.has("data") ? root.get("data") : root;
            LearningAiSemanticResponse response = objectMapper.treeToValue(payload, LearningAiSemanticResponse.class);

            if (response == null || response.results() == null) {
                return List.of();
            }

            return response.results().stream()
                .map(result -> new LearningAiSemanticHit(
                    result.chunkId(),
                    result.noteId(),
                    result.content(),
                    result.score()
                ))
                .toList();
        } catch (RestClientException | java.io.IOException ex) {
            throw new IllegalStateException("learning-ai 시맨틱 검색 호출에 실패했습니다", ex);
        }
    }

    public record LearningAiSemanticHit(
        UUID chunkId,
        UUID noteId,
        String content,
        Float score
    ) {
    }
}
