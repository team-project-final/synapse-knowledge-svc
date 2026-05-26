package com.synapse.knowledge.search.dto;

import java.util.List;

public record SemanticSearchResponse(
    List<UnifiedSearchResultResponse> results,
    long totalCount,
    long searchTimeMs
) {
}
