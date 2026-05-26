package com.synapse.knowledge.search.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.knowledge.search.dto.client.LearningAiSemanticRequest;
import com.synapse.knowledge.search.dto.client.LearningAiSemanticResponse;
import com.synapse.knowledge.search.service.support.SearchCandidate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class LearningAiSearchClient {

    private static final String SEMANTIC_SEARCH_PATH = "/api/v1/ai/search/semantic";

    @Qualifier("learningAiRestClient")
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public List<SearchCandidate> searchSemantic(Long userId, String query, int limit, List<String> tags) {
        try {
            String responseBody = restClient.post()
                .uri(SEMANTIC_SEARCH_PATH)
                .header("X-User-Id", String.valueOf(userId))
                .body(new LearningAiSemanticRequest(query, limit, tags))
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

            List<SearchCandidate> candidates = new ArrayList<>();
            for (LearningAiSemanticResponse.LearningAiSemanticResult result : response.results()) {
                candidates.add(new SearchCandidate(
                    result.noteId(),
                    result.title(),
                    result.highlights() == null ? List.of() : result.highlights(),
                    result.snippet(),
                    null,
                    result.score()
                ));
            }
            return candidates;
        } catch (RestClientException | java.io.IOException ex) {
            throw new IllegalStateException("learning-ai 시맨틱 검색 호출에 실패했습니다", ex);
        }
    }
}
