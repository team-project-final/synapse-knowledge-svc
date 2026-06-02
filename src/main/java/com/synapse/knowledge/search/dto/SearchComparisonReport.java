package com.synapse.knowledge.search.dto;

import java.time.Instant;
import java.util.List;

public record SearchComparisonReport(
    Instant generatedAt,
    String datasetVersion,
    boolean semanticAvailable,
    SearchAccuracyReport bm25,
    SearchAccuracyReport semantic,
    SearchAccuracyReport hybrid,
    List<String> improvements
) {
}
