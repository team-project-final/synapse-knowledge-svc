package com.synapse.knowledge.search.dto.client;

import java.util.List;

public record LearningAiSemanticRequest(
    String query,
    int limit,
    List<String> tags
) {
}
