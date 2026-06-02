package com.synapse.knowledge.search.dto;

import java.util.UUID;
import java.util.List;

public record SemanticSearchResponse(
    List<SemanticSearchResult> results,
    long totalCount,
    long searchTimeMs
) {
    public record SemanticSearchResult(
        UUID chunkId,
        UUID noteId,
        String content,
        Float score
    ) {
    }
}
