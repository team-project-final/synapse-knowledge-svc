package com.synapse.knowledge.shared;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record NoteSearchSyncRequested(
    Long noteId,
    UUID externalNoteId,
    String tenantId,
    Long userId,
    String title,
    String contentPlain,
    List<String> tags,
    boolean deleted,
    Instant requestedAt
) {
}
