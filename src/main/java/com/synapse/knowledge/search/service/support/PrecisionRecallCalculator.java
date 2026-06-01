package com.synapse.knowledge.search.service.support;

import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class PrecisionRecallCalculator {

    public double precisionAtK(List<Long> actualNoteIds, Set<Long> relevantNoteIds, int k) {
        if (k <= 0 || actualNoteIds.isEmpty()) {
            return 0.0d;
        }

        long relevantCount = actualNoteIds.stream()
            .limit(k)
            .filter(relevantNoteIds::contains)
            .count();
        return (double) relevantCount / k;
    }

    public double recallAtK(List<Long> actualNoteIds, Set<Long> relevantNoteIds, int k) {
        if (relevantNoteIds.isEmpty()) {
            return 0.0d;
        }

        long relevantCount = actualNoteIds.stream()
            .limit(k)
            .filter(relevantNoteIds::contains)
            .count();
        return (double) relevantCount / relevantNoteIds.size();
    }
}
