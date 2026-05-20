package com.synapse.knowledge.chunking.application;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
class TokenCounter {

    int countTokens(String text) {
        return tokenize(text).size();
    }

    List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        return Arrays.stream(text.trim().split("\\s+"))
            .filter(token -> !token.isBlank())
            .toList();
    }
}
