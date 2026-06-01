package com.synapse.knowledge.note.kafka.producer;

import com.synapse.knowledge.note.entity.Note;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record NoteCreatedPublishRequested(
    String eventId,
    UUID externalNoteId,
    String userId,
    String tenantId,
    String title,
    String content,
    String createdAt,
    Long occurredAt,
    String deckId
) {

    public static NoteCreatedPublishRequested from(Note note, UUID externalNoteId, String eventUserId) {
        Instant now = Instant.now();
        return new NoteCreatedPublishRequested(
            UUID.randomUUID().toString(),
            externalNoteId,
            eventUserId,
            note.getTenantId(),
            note.getTitle(),
            note.getContentPlain(),
            resolveTimestamp(note.getCreatedAt(), now),
            now.toEpochMilli(),
            null
        );
    }

    private static String resolveTimestamp(LocalDateTime value, Instant fallback) {
        return value != null ? value.toString() : fallback.toString();
    }
}
