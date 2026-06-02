package com.synapse.knowledge.search.dto;

import java.util.List;

public record HybridSearchResponse(
    List<UnifiedSearchResultResponse> results,
    long totalCount,
    String nextCursor,
    boolean hasNext,
    long searchTimeMs,
    boolean semanticFallback
) {
    public static HybridSearchResponse of(
        List<UnifiedSearchResultResponse> results,
        long searchTimeMs,
        boolean semanticFallback
    ) {
        return new HybridSearchResponse(
            results,
            results.size(),
            null,
            false,
            searchTimeMs,
            semanticFallback
        );
    }
}
