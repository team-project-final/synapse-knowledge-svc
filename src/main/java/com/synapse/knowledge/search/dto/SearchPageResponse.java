package com.synapse.knowledge.search.dto;

import java.util.List;

public record SearchPageResponse(
    List<SearchResultResponse> results,
    long totalCount,
    String nextCursor,
    boolean hasNext
) {
}
