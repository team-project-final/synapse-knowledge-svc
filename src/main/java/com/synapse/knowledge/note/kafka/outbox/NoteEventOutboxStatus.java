package com.synapse.knowledge.note.kafka.outbox;

public enum NoteEventOutboxStatus {
    PENDING,
    IN_PROGRESS,
    PUBLISHED
}
