package com.synapse.knowledge.search.dto;

import java.util.List;

public record SearchTestQuery(
    String query,
    List<Long> expectedNoteIds,
    List<Integer> relevanceScores
) {
}
