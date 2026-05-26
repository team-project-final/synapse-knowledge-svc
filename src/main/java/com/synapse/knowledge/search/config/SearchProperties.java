package com.synapse.knowledge.search.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "search")
public record SearchProperties(
    Ai ai,
    Hybrid hybrid
) {
    public record Ai(String baseUrl, Duration timeout) {
    }

    public record Hybrid(int rrfK, int candidateMultiplier) {
    }
}
