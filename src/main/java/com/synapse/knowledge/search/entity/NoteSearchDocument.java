package com.synapse.knowledge.search.entity;

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
