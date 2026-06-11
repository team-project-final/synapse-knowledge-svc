package com.synapse.knowledge.chunking.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "chunking.ai")
public record ChunkingAiProperties(
    boolean enabled,
    @NotBlank String baseUrl,
    @NotNull Duration timeout,
    @Positive int expectedDimensions
) {
}
