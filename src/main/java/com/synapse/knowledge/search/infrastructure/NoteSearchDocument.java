package com.synapse.knowledge.search.infrastructure;

import java.time.Instant;
import java.util.List;

public record NoteSearchDocument(
    Long noteId,
    String tenantId,
    Long userId,
    String title,
    String content,
    List<String> tags,
    Instant updatedAt
) {
}
