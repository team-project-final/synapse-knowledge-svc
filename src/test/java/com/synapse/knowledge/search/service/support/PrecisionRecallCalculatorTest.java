package com.synapse.knowledge.search.service.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PrecisionRecallCalculatorTest {

    private final PrecisionRecallCalculator calculator = new PrecisionRecallCalculator();

    @Test
    @DisplayName("상위 K 결과 중 관련 문서 비율로 정확도를 반환한다")
    void precisionAtK_relevantDocRatioAmongTopK_shouldReturnPrecision() {
        double precision = calculator.precisionAtK(List.of(10L, 20L, 30L, 40L), Set.of(10L, 30L), 3);

        assertThat(precision).isEqualTo(2.0d / 3.0d);
    }

    @Test
    @DisplayName("관련 문서 회수 비율로 재현율을 반환한다")
    void recallAtK_relevantDocRecallRatio_shouldReturnRecall() {
        double recall = calculator.recallAtK(List.of(10L, 20L, 30L, 40L), Set.of(10L, 30L, 50L), 3);

        assertThat(recall).isEqualTo(2.0d / 3.0d);
    }
}
