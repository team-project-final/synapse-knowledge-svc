package com.synapse.knowledge.search.entity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record NoteSearchDocument(
    Long noteId,
    UUID externalNoteId,
    String tenantId,
    Long userId,
    String title,
    String content,
    List<String> tags,
    Instant updatedAt
) {
}
