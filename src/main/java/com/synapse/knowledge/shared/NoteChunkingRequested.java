package com.synapse.knowledge.shared;

import java.time.Instant;

public record NoteChunkingRequested(
    Long noteId,
    String tenantId,
    String contentPlain,
    String reason,
    Instant occurredAt
) {
}
