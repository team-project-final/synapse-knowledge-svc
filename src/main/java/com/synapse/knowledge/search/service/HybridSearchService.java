package com.synapse.knowledge.search.service;

import com.synapse.knowledge.search.SearchIdentity;
import com.synapse.knowledge.search.client.LearningAiSearchClient;
import com.synapse.knowledge.search.config.SearchProperties;
import com.synapse.knowledge.search.dto.HybridSearchRequest;
import com.synapse.knowledge.search.dto.HybridSearchResponse;
import com.synapse.knowledge.search.dto.UnifiedSearchResultResponse;
import com.synapse.knowledge.search.repository.NoteSearchRepository;
import com.synapse.knowledge.search.service.support.SearchCandidate;
import com.synapse.knowledge.shared.NoteIdentityQueryPort;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HybridSearchService {

    private final NoteSearchRepository noteSearchRepository;
    private final LearningAiSearchClient learningAiSearchClient;
    private final RrfMergeService rrfMergeService;
    private final SearchProperties searchProperties;
    private final NoteIdentityQueryPort noteIdentityQueryPort;

    public HybridSearchResponse search(SearchIdentity identity, HybridSearchRequest request) {
        Instant startedAt = Instant.now();
        int candidateLimit = Math.max(
            request.limit(),
            request.limit() * Math.max(1, searchProperties.hybrid().candidateMultiplier())
        );

        CompletableFuture<List<SearchCandidate>> keywordFuture = CompletableFuture.supplyAsync(() ->
            noteSearchRepository.searchKeywordCandidates(identity.userId(), request.query(), candidateLimit, request.tags())
        );
        CompletableFuture<List<LearningAiSearchClient.LearningAiSemanticHit>> semanticFuture =
            identity.canUseSemanticSearch()
                ? CompletableFuture.supplyAsync(() ->
                    learningAiSearchClient.searchSemantic(identity.semanticActorId(), request.query(), candidateLimit))
                : CompletableFuture.completedFuture(List.of());

        List<SearchCandidate> keywordResults = keywordFuture.join();
        List<SearchCandidate> semanticResults = List.of();
        boolean semanticFallback = !identity.canUseSemanticSearch();

        try {
            List<LearningAiSearchClient.LearningAiSemanticHit> semanticHits =
                semanticFuture.get(searchProperties.ai().timeout().toMillis(), TimeUnit.MILLISECONDS);
            semanticResults = toMergeableCandidates(semanticHits);
            if (!semanticHits.isEmpty() && semanticResults.isEmpty()) {
                semanticFallback = true;
            }
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

    private List<SearchCandidate> toMergeableCandidates(List<LearningAiSearchClient.LearningAiSemanticHit> semanticHits) {
        if (semanticHits.isEmpty()) {
            return List.of();
        }

        return semanticHits.stream()
            .map(hit -> noteIdentityQueryPort.findByExternalNoteId(hit.noteId())
                .map(noteIdentity -> new SearchCandidate(
                    noteIdentity.noteId(),
                    noteIdentity.externalNoteId(),
                    noteIdentity.title(),
                    List.of(),
                    hit.content(),
                    null,
                    hit.score()
                ))
                .orElseGet(() -> {
                    log.warn("semantic hit noteId={} has no knowledge note mapping", hit.noteId());
                    return null;
                }))
            .filter(Objects::nonNull)
            .toList();
    }
}
