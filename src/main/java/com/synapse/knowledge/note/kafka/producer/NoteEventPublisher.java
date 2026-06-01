package com.synapse.knowledge.note.kafka.producer;

import com.synapse.knowledge.NoteCreated;
import com.synapse.knowledge.NoteUpdated;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class NoteEventPublisher {

    private final KafkaTemplate<String, SpecificRecord> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NoteCreatedPublishRequested event) {
        NoteCreated payload = NoteCreated.newBuilder()
            .setEventId(event.eventId())
            .setNoteId(event.externalNoteId().toString())
            .setUserId(String.valueOf(event.userId()))
            .setTenantId(event.tenantId())
            .setDeckId(event.deckId())
            .setTitle(event.title())
            .setContent(event.content())
            .setCreatedAt(event.createdAt())
            .setOccurredAt(event.occurredAt())
            .build();

        send(NoteKafkaTopics.NOTE_CREATED, event.tenantId(), payload);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NoteUpdatedPublishRequested event) {
        NoteUpdated payload = NoteUpdated.newBuilder()
            .setEventId(event.eventId())
            .setNoteId(event.externalNoteId().toString())
            .setUserId(String.valueOf(event.userId()))
            .setTenantId(event.tenantId())
            .setTitle(event.title())
            .setUpdatedAt(event.updatedAt())
            .setOccurredAt(event.occurredAt())
            .build();

        send(NoteKafkaTopics.NOTE_UPDATED, event.tenantId(), payload);
    }

    private void send(String topic, String key, SpecificRecord payload) {
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
    }
}
