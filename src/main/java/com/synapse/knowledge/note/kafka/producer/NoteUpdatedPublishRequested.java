package com.synapse.knowledge.note.kafka.producer;

import com.synapse.knowledge.note.entity.Note;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record NoteUpdatedPublishRequested(
    String eventId,
    UUID externalNoteId,
    Long userId,
    String tenantId,
    String title,
    String updatedAt,
    Long occurredAt
) {

    public static NoteUpdatedPublishRequested from(Note note, UUID externalNoteId) {
        Instant now = Instant.now();
        return new NoteUpdatedPublishRequested(
            UUID.randomUUID().toString(),
            externalNoteId,
            note.getUserId(),
            note.getTenantId(),
            note.getTitle(),
            resolveTimestamp(note.getUpdatedAt(), now),
            now.toEpochMilli()
        );
    }

    private static String resolveTimestamp(LocalDateTime value, Instant fallback) {
        return value != null ? value.toString() : fallback.toString();
    }
}
