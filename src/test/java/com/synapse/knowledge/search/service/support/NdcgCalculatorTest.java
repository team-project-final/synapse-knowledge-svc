package com.synapse.knowledge.search.service.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NdcgCalculatorTest {

    private final NdcgCalculator calculator = new NdcgCalculator();

    @Test
    @DisplayName("ndcgAtK_이상적인순위와같으면_should1을반환")
    void ndcgAtK_이상적인순위와같으면_should1을반환() {
        double ndcg = calculator.ndcgAtK(
            List.of(101L, 202L, 303L),
            Map.of(101L, 2, 202L, 1, 303L, 1),
            3
        );

        assertThat(ndcg).isEqualTo(1.0d);
    }

    @Test
    @DisplayName("ndcgAtK_관련문서가뒤로밀리면_should1보다작다")
    void ndcgAtK_관련문서가뒤로밀리면_should1보다작다() {
        double ndcg = calculator.ndcgAtK(
            List.of(202L, 101L, 303L),
            Map.of(101L, 2, 202L, 1, 303L, 1),
            3
        );

        assertThat(ndcg).isLessThan(1.0d);
    }
}
