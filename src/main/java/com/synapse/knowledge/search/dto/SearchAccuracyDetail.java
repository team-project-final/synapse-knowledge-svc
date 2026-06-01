package com.synapse.knowledge.search.dto;

import java.util.List;

public record SearchAccuracyDetail(
    String query,
    List<Long> expectedNoteIds,
    List<Long> actualTopNoteIds,
    double precisionAt10,
    double recallAt10,
    double reciprocalRank,
    double ndcgAt10
) {
}
