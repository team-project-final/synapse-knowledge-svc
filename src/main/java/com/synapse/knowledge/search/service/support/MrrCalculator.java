package com.synapse.knowledge.search.service.support;

import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class MrrCalculator {

    public double reciprocalRank(List<Long> actualNoteIds, Set<Long> relevantNoteIds) {
        for (int i = 0; i < actualNoteIds.size(); i++) {
            if (relevantNoteIds.contains(actualNoteIds.get(i))) {
                return 1.0d / (i + 1);
            }
        }
        return 0.0d;
    }

    public double meanReciprocalRank(List<Double> reciprocalRanks) {
        if (reciprocalRanks.isEmpty()) {
            return 0.0d;
        }
        return reciprocalRanks.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0d);
    }
}
