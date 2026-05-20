package com.synapse.knowledge.search.dto;

import java.util.List;

public record SearchRequest(
    String query,
    String cursor,
    int limit,
    List<String> tags
) {
}
