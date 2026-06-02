package com.synapse.knowledge.search.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LearningAiSemanticRequest(
    String query,
    @JsonProperty("top_k")
    int topK,
    double threshold
) {
}
