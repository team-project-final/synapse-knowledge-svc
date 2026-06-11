package com.synapse.knowledge.chunking.service.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenCounterTest {

    private final TokenCounter tokenCounter = new TokenCounter();

    @Test
    @DisplayName("tokenize_빈문자열_should빈리스트반환")
    void tokenize_빈문자열_should빈리스트반환() {
        assertThat(tokenCounter.tokenize("   ")).isEmpty();
        assertThat(tokenCounter.tokenize(null)).isEmpty();
    }

    @Test
    @DisplayName("tokenize_여러공백과개행포함_should공백기준으로분리")
    void tokenize_여러공백과개행포함_should공백기준으로분리() {
        List<String> tokens = tokenCounter.tokenize(" alpha   beta\n gamma \t delta ");

        assertThat(tokens).containsExactly("alpha", "beta", "gamma", "delta");
    }

    @Test
    @DisplayName("countTokens_일반문장_should토큰개수를반환")
    void countTokens_일반문장_should토큰개수를반환() {
        assertThat(tokenCounter.countTokens("하나 둘 셋")).isEqualTo(3);
    }
}
