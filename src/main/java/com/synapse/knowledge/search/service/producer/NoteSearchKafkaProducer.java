package com.synapse.knowledge.search.service.producer;

import com.synapse.knowledge.search.event.NoteSearchSyncKafkaEvent;
import com.synapse.knowledge.shared.NoteSearchSyncRequested;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
class NoteSearchKafkaProducer {

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onNoteSearchSyncRequested(NoteSearchSyncRequested event) {
        NoteSearchSyncKafkaEvent kafkaEvent = NoteSearchSyncKafkaEvent.from(event);
        kafkaTemplate.send(NoteSearchSyncKafkaEvent.TOPIC, kafkaEvent.noteId().toString(), kafkaEvent);
        log.debug("Kafka event sent: topic={}, noteId={}", NoteSearchSyncKafkaEvent.TOPIC, kafkaEvent.noteId());
    }
}
