package com.synapse.knowledge.search.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "search")
public record SearchProperties(
    @NotNull @Valid Ai ai,
    @NotNull @Valid Hybrid hybrid
) {
    public record Ai(
        @NotBlank String baseUrl,
        @NotNull Duration timeout,
        @DecimalMin("0.0") @DecimalMax("1.0") double threshold
    ) {
    }

    public record Hybrid(
        @Positive int rrfK,
        @Positive int candidateMultiplier
    ) {
    }
}
