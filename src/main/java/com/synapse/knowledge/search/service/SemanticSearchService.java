package com.synapse.knowledge.search.service;

import com.synapse.knowledge.search.client.LearningAiSearchClient;
import com.synapse.knowledge.search.dto.SemanticSearchRequest;
import com.synapse.knowledge.search.dto.SemanticSearchResponse;
import com.synapse.knowledge.search.dto.UnifiedSearchResultResponse;
import com.synapse.knowledge.search.service.support.SearchCandidate;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SemanticSearchService {

    private final LearningAiSearchClient learningAiSearchClient;

    public SemanticSearchResponse search(Long userId, SemanticSearchRequest request) {
        Instant startedAt = Instant.now();
        List<SearchCandidate> candidates = learningAiSearchClient.searchSemantic(
            userId,
            request.query(),
            request.limit(),
            request.tags()
        );

        List<UnifiedSearchResultResponse> results = candidates.stream()
            .map(candidate -> candidate.toResponse(candidate.semanticScore() == null ? 0.0f : candidate.semanticScore()))
            .toList();

        return new SemanticSearchResponse(
            results,
            results.size(),
            Duration.between(startedAt, Instant.now()).toMillis()
        );
    }
}
