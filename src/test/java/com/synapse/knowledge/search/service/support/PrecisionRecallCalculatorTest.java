package com.synapse.knowledge.search.service.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PrecisionRecallCalculatorTest {

    private final PrecisionRecallCalculator calculator = new PrecisionRecallCalculator();

    @Test
    @DisplayName("precisionAtK_상위K결과중관련문서비율_should정확도를반환")
    void precisionAtK_상위K결과중관련문서비율_should정확도를반환() {
        double precision = calculator.precisionAtK(List.of(10L, 20L, 30L, 40L), Set.of(10L, 30L), 3);

        assertThat(precision).isEqualTo(2.0d / 3.0d);
    }

    @Test
    @DisplayName("recallAtK_관련문서회수비율_should재현율을반환")
    void recallAtK_관련문서회수비율_should재현율을반환() {
        double recall = calculator.recallAtK(List.of(10L, 20L, 30L, 40L), Set.of(10L, 30L, 50L), 3);

        assertThat(recall).isEqualTo(2.0d / 3.0d);
    }
}
