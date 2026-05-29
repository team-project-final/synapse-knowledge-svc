package com.synapse.knowledge.search.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record SemanticSearchRequest(
    @NotBlank String query,
    @Min(1) @Max(100) int limit,
    List<@Size(max = 30) String> tags
) {
}
