package com.synapse.knowledge.search.dto;

import com.synapse.knowledge.search.service.support.SearchMode;
import java.util.List;

public record SearchAccuracyReport(
    SearchMode mode,
    int queryCount,
    double precisionAt10,
    double recallAt10,
    double mrr,
    double ndcgAt10,
    List<SearchAccuracyDetail> details
) {
    public static SearchAccuracyReport empty(SearchMode mode, int queryCount) {
        return new SearchAccuracyReport(mode, queryCount, 0.0d, 0.0d, 0.0d, 0.0d, List.of());
    }
}
