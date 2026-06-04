package com.synapse.knowledge.search.service.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MrrCalculatorTest {

    private final MrrCalculator calculator = new MrrCalculator();

    @Test
    @DisplayName("첫 관련 문서 순위가 있으면 역순위를 반환한다")
    void reciprocalRank_firstRelevantDocExists_shouldReturnReciprocalRank() {
        double reciprocalRank = calculator.reciprocalRank(List.of(11L, 22L, 33L), Set.of(33L));

        assertThat(reciprocalRank).isEqualTo(1.0d / 3.0d);
    }

    @Test
    @DisplayName("여러 쿼리 점수 평균으로 MRR을 반환한다")
    void meanReciprocalRank_averageOfMultipleQueryScores_shouldReturnMrr() {
        double mrr = calculator.meanReciprocalRank(List.of(1.0d, 0.5d, 0.0d));

        assertThat(mrr).isEqualTo(0.5d);
    }
}
