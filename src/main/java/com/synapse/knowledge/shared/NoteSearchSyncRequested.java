package com.synapse.knowledge.shared;

import java.time.Instant;
import java.util.List;

public record NoteSearchSyncRequested(
    Long noteId,
    String tenantId,
    Long userId,
    String title,
    String contentPlain,
    List<String> tags,
    boolean deleted,
    Instant requestedAt
) {
}
