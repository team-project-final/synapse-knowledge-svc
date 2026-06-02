package com.synapse.knowledge.search.service.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MrrCalculatorTest {

    private final MrrCalculator calculator = new MrrCalculator();

    @Test
    @DisplayName("reciprocalRank_첫관련문서순위가있으면_should역순위를반환")
    void reciprocalRank_첫관련문서순위가있으면_should역순위를반환() {
        double reciprocalRank = calculator.reciprocalRank(List.of(11L, 22L, 33L), Set.of(33L));

        assertThat(reciprocalRank).isEqualTo(1.0d / 3.0d);
    }

    @Test
    @DisplayName("meanReciprocalRank_여러쿼리점수평균_shouldMRR을반환")
    void meanReciprocalRank_여러쿼리점수평균_shouldMRR을반환() {
        double mrr = calculator.meanReciprocalRank(List.of(1.0d, 0.5d, 0.0d));

        assertThat(mrr).isEqualTo(0.5d);
    }
}
