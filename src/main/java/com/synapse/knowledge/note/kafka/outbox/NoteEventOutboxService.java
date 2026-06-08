package com.synapse.knowledge.note.kafka.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.knowledge.note.entity.Note;
import com.synapse.knowledge.note.kafka.producer.NoteCreatedPublishRequested;
import com.synapse.knowledge.note.kafka.producer.NoteKafkaTopics;
import com.synapse.knowledge.note.kafka.producer.NoteUpdatedPublishRequested;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteEventOutboxService {

    static final String EVENT_TYPE_CREATED = "NOTE_CREATED";
    static final String EVENT_TYPE_UPDATED = "NOTE_UPDATED";

    private final NoteEventOutboxRepository noteEventOutboxRepository;
    private final ObjectMapper objectMapper;

    @Value("${synapse.kafka.enabled:false}")
    private boolean kafkaEnabled;

    public void enqueueCreated(Note note, UUID externalNoteId, String eventUserId) {
        if (!kafkaEnabled) {
            log.debug("Kafka disabled, skipping note created outbox enqueue noteId={}", note.getId());
            return;
        }
        NoteCreatedPublishRequested payload = NoteCreatedPublishRequested.from(note, externalNoteId, eventUserId);
        enqueue(
            payload.eventId(),
            NoteKafkaTopics.NOTE_CREATED,
            note.getTenantId(),
            EVENT_TYPE_CREATED,
            serialize(payload)
        );
    }

    public void enqueueUpdated(Note note, UUID externalNoteId, String eventUserId) {
        if (!kafkaEnabled) {
            log.debug("Kafka disabled, skipping note updated outbox enqueue noteId={}", note.getId());
            return;
        }
        NoteUpdatedPublishRequested payload = NoteUpdatedPublishRequested.from(note, externalNoteId, eventUserId);
        enqueue(
            payload.eventId(),
            NoteKafkaTopics.NOTE_UPDATED,
            note.getTenantId(),
            EVENT_TYPE_UPDATED,
            serialize(payload)
        );
    }

    private void enqueue(String eventId, String topic, String messageKey, String eventType, String payloadJson) {
        NoteEventOutbox outbox = NoteEventOutbox.pending(eventId, topic, messageKey, eventType, payloadJson);
        try {
            noteEventOutboxRepository.save(outbox);
        } catch (DataIntegrityViolationException ex) {
            log.info("Duplicate outbox event skipped eventId={}, topic={}", eventId, topic);
        }
    }

    private String serialize(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Kafka outbox payload serialization failed", ex);
        }
    }
}
