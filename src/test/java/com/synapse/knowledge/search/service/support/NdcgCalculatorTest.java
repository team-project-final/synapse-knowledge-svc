package com.synapse.knowledge.search.service.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NdcgCalculatorTest {

    private final NdcgCalculator calculator = new NdcgCalculator();

    @Test
    @DisplayName("이상적인 순위와 같으면 1을 반환한다")
    void ndcgAtK_idealRanking_shouldReturn1() {
        double ndcg = calculator.ndcgAtK(
            List.of(101L, 202L, 303L),
            Map.of(101L, 2, 202L, 1, 303L, 1),
            3
        );

        assertThat(ndcg).isEqualTo(1.0d);
    }

    @Test
    @DisplayName("관련 문서가 뒤로 밀리면 1보다 작다")
    void ndcgAtK_relevantDocsRankedLower_shouldBeLessThan1() {
        double ndcg = calculator.ndcgAtK(
            List.of(202L, 101L, 303L),
            Map.of(101L, 2, 202L, 1, 303L, 1),
            3
        );

        assertThat(ndcg).isLessThan(1.0d);
    }
}
