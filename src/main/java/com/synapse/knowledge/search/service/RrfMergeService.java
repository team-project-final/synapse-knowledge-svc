package com.synapse.knowledge.search.service;

import com.synapse.knowledge.search.dto.UnifiedSearchResultResponse;
import com.synapse.knowledge.search.service.support.SearchCandidate;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RrfMergeService {

    public List<UnifiedSearchResultResponse> merge(
        List<SearchCandidate> keywordResults,
        List<SearchCandidate> semanticResults,
        int limit,
        int rrfK
    ) {
        Map<UUID, MutableResult> merged = new LinkedHashMap<>();
        applyRanks(merged, keywordResults, rrfK, true);
        applyRanks(merged, semanticResults, rrfK, false);

        return merged.values().stream()
            .sorted((left, right) -> Float.compare(right.rrfScore, left.rrfScore))
            .limit(limit)
            .map(MutableResult::toResponse)
            .toList();
    }

    private void applyRanks(
        Map<UUID, MutableResult> merged,
        List<SearchCandidate> results,
        int rrfK,
        boolean keywordSource
    ) {
        Set<UUID> seenNoteIds = new HashSet<>();

        for (int index = 0; index < results.size(); index++) {
            SearchCandidate candidate = results.get(index);
            if (candidate.externalNoteId() == null || !seenNoteIds.add(candidate.externalNoteId())) {
                continue;
            }

            MutableResult result = merged.computeIfAbsent(candidate.externalNoteId(), ignored -> new MutableResult(candidate));
            result.merge(candidate);
            result.rrfScore += (float) (1.0d / (rrfK + index + 1));
            if (keywordSource) {
                result.keywordScore = candidate.keywordScore();
            } else {
                result.semanticScore = candidate.semanticScore();
            }
        }
    }

    private static final class MutableResult {
        private final Long noteId;
        private String title;
        private List<String> highlights;
        private String snippet;
        private Float keywordScore;
        private Float semanticScore;
        private float rrfScore;

        private MutableResult(SearchCandidate candidate) {
            this.noteId = candidate.noteId();
            this.title = candidate.title();
            this.highlights = candidate.highlights();
            this.snippet = candidate.snippet();
            this.keywordScore = candidate.keywordScore();
            this.semanticScore = candidate.semanticScore();
        }

        private void merge(SearchCandidate candidate) {
            if (title == null) {
                title = candidate.title();
            }
            if ((highlights == null || highlights.isEmpty()) && candidate.highlights() != null && !candidate.highlights().isEmpty()) {
                highlights = candidate.highlights();
            }
            if ((snippet == null || snippet.isBlank()) && candidate.snippet() != null && !candidate.snippet().isBlank()) {
                snippet = candidate.snippet();
            }
            if (keywordScore == null) {
                keywordScore = candidate.keywordScore();
            }
            if (semanticScore == null) {
                semanticScore = candidate.semanticScore();
            }
        }

        private UnifiedSearchResultResponse toResponse() {
            return new UnifiedSearchResultResponse(
                noteId,
                title,
                highlights == null ? List.of() : highlights,
                snippet,
                keywordScore,
                semanticScore,
                rrfScore
            );
        }
    }
}
