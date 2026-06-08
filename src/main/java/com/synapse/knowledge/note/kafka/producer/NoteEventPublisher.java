package com.synapse.knowledge.note.kafka.producer;

import com.synapse.knowledge.NoteCreated;
import com.synapse.knowledge.NoteUpdated;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@ConditionalOnProperty(prefix = "synapse.kafka", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class NoteEventPublisher {

    private final KafkaTemplate<String, SpecificRecord> kafkaTemplate;

    public CompletableFuture<SendResult<String, SpecificRecord>> publishCreated(NoteCreatedPublishRequested event) {
        NoteCreated payload = NoteCreated.newBuilder()
            .setEventId(event.eventId())
            .setNoteId(event.externalNoteId().toString())
            .setUserId(event.userId())
            .setTenantId(event.tenantId())
            .setDeckId(event.deckId())
            .setTitle(event.title())
            .setContent(event.content())
            .setCreatedAt(event.createdAt())
            .setOccurredAt(event.occurredAt())
            .build();

        return send(NoteKafkaTopics.NOTE_CREATED, event.tenantId(), payload);
    }

    public CompletableFuture<SendResult<String, SpecificRecord>> publishUpdated(NoteUpdatedPublishRequested event) {
        NoteUpdated payload = NoteUpdated.newBuilder()
            .setEventId(event.eventId())
            .setNoteId(event.externalNoteId().toString())
            .setUserId(event.userId())
            .setTenantId(event.tenantId())
            .setTitle(event.title())
            .setUpdatedAt(event.updatedAt())
            .setOccurredAt(event.occurredAt())
            .build();

        return send(NoteKafkaTopics.NOTE_UPDATED, event.tenantId(), payload);
    }

    private CompletableFuture<SendResult<String, SpecificRecord>> send(String topic, String key, SpecificRecord payload) {
        CompletableFuture<SendResult<String, SpecificRecord>> future = kafkaTemplate.send(topic, key, payload);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Kafka publish failed topic={}, key={}, message={}", topic, key, ex.getMessage(), ex);
                return;
            }

            if (result != null) {
                log.debug(
                    "Kafka publish succeeded topic={}, key={}, partition={}, offset={}",
                    topic,
                    key,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
                );
            }
        });
        return future;
    }
}
