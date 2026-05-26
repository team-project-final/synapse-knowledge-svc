package com.synapse.knowledge.search.service;

import com.synapse.knowledge.search.client.LearningAiSearchClient;
import com.synapse.knowledge.search.config.SearchProperties;
import com.synapse.knowledge.search.dto.HybridSearchRequest;
import com.synapse.knowledge.search.dto.HybridSearchResponse;
import com.synapse.knowledge.search.dto.UnifiedSearchResultResponse;
import com.synapse.knowledge.search.repository.NoteSearchRepository;
import com.synapse.knowledge.search.service.support.SearchCandidate;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HybridSearchService {

    private final NoteSearchRepository noteSearchRepository;
    private final LearningAiSearchClient learningAiSearchClient;
    private final RrfMergeService rrfMergeService;
    private final SearchProperties searchProperties;

    public HybridSearchResponse search(Long userId, HybridSearchRequest request) {
        Instant startedAt = Instant.now();
        int candidateLimit = Math.max(
            request.limit(),
            request.limit() * Math.max(1, searchProperties.hybrid().candidateMultiplier())
        );

        CompletableFuture<List<SearchCandidate>> keywordFuture = CompletableFuture.supplyAsync(() ->
            noteSearchRepository.searchKeywordCandidates(userId, request.query(), candidateLimit, request.tags())
        );
        CompletableFuture<List<SearchCandidate>> semanticFuture = CompletableFuture.supplyAsync(() ->
            learningAiSearchClient.searchSemantic(userId, request.query(), candidateLimit, request.tags())
        );

        List<SearchCandidate> keywordResults = keywordFuture.join();
        List<SearchCandidate> semanticResults = List.of();
        boolean semanticFallback = false;

        try {
            semanticResults = semanticFuture.get(searchProperties.ai().timeout().toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception ex) {
            semanticFallback = true;
        }

        List<UnifiedSearchResultResponse> results = rrfMergeService.merge(
            keywordResults,
            semanticResults,
            request.limit(),
            searchProperties.hybrid().rrfK()
        );

        return HybridSearchResponse.of(
            results,
            Duration.between(startedAt, Instant.now()).toMillis(),
            semanticFallback
        );
    }
}
