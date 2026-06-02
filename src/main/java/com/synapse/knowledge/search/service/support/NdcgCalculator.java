package com.synapse.knowledge.search.service.support;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class NdcgCalculator {

    public double ndcgAtK(List<Long> actualNoteIds, Map<Long, Integer> relevanceByNoteId, int k) {
        double dcg = dcg(actualNoteIds.stream().limit(k).toList(), relevanceByNoteId);
        double idcg = idealDcg(relevanceByNoteId, k);

        if (idcg == 0.0d) {
            return 0.0d;
        }
        return dcg / idcg;
    }

    public double average(List<Double> scores) {
        if (scores.isEmpty()) {
            return 0.0d;
        }
        return scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0d);
    }

    private double dcg(List<Long> rankedNoteIds, Map<Long, Integer> relevanceByNoteId) {
        double score = 0.0d;
        for (int i = 0; i < rankedNoteIds.size(); i++) {
            int relevance = relevanceByNoteId.getOrDefault(rankedNoteIds.get(i), 0);
            if (relevance == 0) {
                continue;
            }
            score += (Math.pow(2.0d, relevance) - 1.0d) / log2(i + 2.0d);
        }
        return score;
    }

    private double idealDcg(Map<Long, Integer> relevanceByNoteId, int k) {
        List<Integer> idealRelevances = relevanceByNoteId.values().stream()
            .sorted(Comparator.reverseOrder())
            .limit(k)
            .toList();

        double score = 0.0d;
        for (int i = 0; i < idealRelevances.size(); i++) {
            int relevance = idealRelevances.get(i);
            score += (Math.pow(2.0d, relevance) - 1.0d) / log2(i + 2.0d);
        }
        return score;
    }

    private double log2(double value) {
        return Math.log(value) / Math.log(2.0d);
    }
}
