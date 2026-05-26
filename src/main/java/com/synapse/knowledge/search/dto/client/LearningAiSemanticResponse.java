package com.synapse.knowledge.search.dto.client;

import java.util.List;

public record LearningAiSemanticResponse(
    List<LearningAiSemanticResult> results,
    long totalCount
) {
    public record LearningAiSemanticResult(
        Long noteId,
        String title,
        String snippet,
        List<String> highlights,
        Float score
    ) {
    }
}
