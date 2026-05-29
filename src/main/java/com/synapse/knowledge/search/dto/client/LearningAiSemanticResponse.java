package com.synapse.knowledge.search.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;

public record LearningAiSemanticResponse(
    List<LearningAiSemanticResult> results
) {
    public record LearningAiSemanticResult(
        @JsonProperty("chunk_id")
        UUID chunkId,
        @JsonProperty("note_id")
        UUID noteId,
        String content,
        Float score
    ) {
    }
}
