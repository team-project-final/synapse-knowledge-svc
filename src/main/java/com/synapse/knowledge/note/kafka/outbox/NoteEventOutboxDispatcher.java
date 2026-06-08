package com.synapse.knowledge.note.kafka.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.knowledge.note.kafka.producer.NoteCreatedPublishRequested;
import com.synapse.knowledge.note.kafka.producer.NoteEventPublisher;
import com.synapse.knowledge.note.kafka.producer.NoteUpdatedPublishRequested;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Component
@Profile("!test")
@ConditionalOnProperty(prefix = "synapse.kafka", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class NoteEventOutboxDispatcher {

    private final NoteEventOutboxRepository noteEventOutboxRepository;
    private final NoteEventOutboxClaimService noteEventOutboxClaimService;
    private final NoteEventPublisher noteEventPublisher;
    private final ObjectMapper objectMapper;

    @Value("${synapse.kafka.outbox.batch-size:50}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${synapse.kafka.outbox.fixed-delay-ms:1000}")
    public void dispatchPending() {
        List<NoteEventOutbox> pendingEvents = noteEventOutboxClaimService.claimNextBatch(batchSize);

        for (NoteEventOutbox outbox : pendingEvents) {
            dispatch(outbox);
        }
    }

    private void dispatch(NoteEventOutbox outbox) {
        try {
            switch (outbox.getEventType()) {
                case NoteEventOutboxService.EVENT_TYPE_CREATED -> noteEventPublisher.publishCreated(
                    objectMapper.readValue(outbox.getPayloadJson(), NoteCreatedPublishRequested.class)
                ).join();
                case NoteEventOutboxService.EVENT_TYPE_UPDATED -> noteEventPublisher.publishUpdated(
                    objectMapper.readValue(outbox.getPayloadJson(), NoteUpdatedPublishRequested.class)
                ).join();
                default -> throw new IllegalStateException("Unsupported outbox event type: " + outbox.getEventType());
            }
            outbox.markPublished();
        } catch (Exception ex) {
            outbox.recordFailure(ex.getMessage());
            log.error(
                "Outbox dispatch failed eventId={}, topic={}, attempts={}",
                outbox.getEventId(),
                outbox.getTopic(),
                outbox.getAttemptCount(),
                ex
            );
        }

        noteEventOutboxRepository.save(outbox);
    }
}
