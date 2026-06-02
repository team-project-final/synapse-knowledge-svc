package com.synapse.knowledge.search.dto;

import java.util.List;

public record UnifiedSearchResultResponse(
    Long noteId,
    String title,
    List<String> highlights,
    String snippet,
    Float keywordScore,
    Float semanticScore,
    float rrfScore
) {
}
