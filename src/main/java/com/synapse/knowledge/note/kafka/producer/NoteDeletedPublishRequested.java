package com.synapse.knowledge.note.kafka.producer;

import com.synapse.knowledge.note.entity.Note;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

public record NoteDeletedPublishRequested(
    String eventId,
    UUID externalNoteId,
    String userId,
    String tenantId,
    String title,
    String deletedAt,
    Long occurredAt
) {

    public static NoteDeletedPublishRequested from(Note note, UUID externalNoteId, String eventUserId) {
        Instant now = Instant.now();
        return new NoteDeletedPublishRequested(
            UUID.randomUUID().toString(),
            externalNoteId,
            eventUserId,
            note.getTenantId(),
            note.getTitle(),
            resolveTimestamp(note.getDeletedAt(), now),
            now.toEpochMilli()
        );
    }

    private static String resolveTimestamp(LocalDateTime value, Instant fallback) {
        long deletedAtMillis = value != null
            ? value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            : fallback.toEpochMilli();
        return String.valueOf(deletedAtMillis);
    }
}
