package com.synapse.knowledge.search.service.support;

import com.synapse.knowledge.search.dto.UnifiedSearchResultResponse;
import java.util.List;

public record SearchCandidate(
    Long noteId,
    String title,
    List<String> highlights,
    String snippet,
    Float keywordScore,
    Float semanticScore
) {
    public UnifiedSearchResultResponse toResponse(float rrfScore) {
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
