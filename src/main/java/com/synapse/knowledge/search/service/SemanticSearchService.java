package com.synapse.knowledge.search.service;

import com.synapse.knowledge.search.SearchIdentity;
import com.synapse.knowledge.search.client.LearningAiSearchClient;
import com.synapse.knowledge.search.dto.SemanticSearchRequest;
import com.synapse.knowledge.search.dto.SemanticSearchResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SemanticSearchService {

    private final LearningAiSearchClient learningAiSearchClient;

    public SemanticSearchResponse search(SearchIdentity identity, SemanticSearchRequest request) {
        if (!identity.canUseSemanticSearch()) {
            throw new IllegalStateException("semantic 검색에는 UUID subject 기반 토큰이 필요합니다");
        }

        Instant startedAt = Instant.now();
        List<LearningAiSearchClient.LearningAiSemanticHit> hits =
            learningAiSearchClient.searchSemantic(identity.semanticActorId(), request.query(), request.limit());

        List<SemanticSearchResponse.SemanticSearchResult> results = hits.stream()
            .map(hit -> new SemanticSearchResponse.SemanticSearchResult(
                hit.chunkId(),
                hit.noteId(),
                hit.content(),
                hit.score()
            ))
            .toList();

        return new SemanticSearchResponse(
            results,
            results.size(),
            Duration.between(startedAt, Instant.now()).toMillis()
        );
    }
}
