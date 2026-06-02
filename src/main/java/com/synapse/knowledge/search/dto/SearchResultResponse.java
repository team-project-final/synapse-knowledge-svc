package com.synapse.knowledge.search.dto;

import java.util.List;

public record SearchResultResponse(
    Long noteId,
    String title,
    List<String> highlights,
    float score
) {
}
