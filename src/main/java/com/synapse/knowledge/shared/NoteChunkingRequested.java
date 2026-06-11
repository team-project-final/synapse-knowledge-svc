package com.synapse.knowledge.shared;

import java.time.Instant;

public record NoteChunkingRequested(
    Long noteId,
    String tenantId,
    String actorId,
    String contentPlain,
    String reason,
    Instant occurredAt
) {
}
