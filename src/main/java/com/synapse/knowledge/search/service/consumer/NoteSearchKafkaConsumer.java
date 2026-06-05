package com.synapse.knowledge.search.service.consumer;

import com.synapse.knowledge.search.entity.NoteSearchDocument;
import com.synapse.knowledge.search.event.NoteSearchSyncKafkaEvent;
import com.synapse.knowledge.search.repository.NoteSearchRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoteSearchKafkaConsumer {

    private final NoteSearchRepository noteSearchRepository;
    private final KafkaIdempotencyStore idempotencyStore;

    @KafkaListener(
        topics = NoteSearchSyncKafkaEvent.TOPIC,
        containerFactory = "searchSyncKafkaListenerContainerFactory"
    )
    public void handle(NoteSearchSyncKafkaEvent event) {
        if (idempotencyStore.isProcessed(event.id())) {
            log.info("Duplicate event skipped: {}", event.id());
            return;
        }

        if (event.deleted()) {
            noteSearchRepository.deleteByNoteId(event.noteId());
        } else {
            noteSearchRepository.upsert(new NoteSearchDocument(
                event.noteId(),
                event.externalNoteId(),
                event.tenantid(),
                event.userId(),
                event.title(),
                event.contentPlain(),
                event.tags() == null ? List.of() : event.tags(),
                Instant.now()
            ));
        }

        idempotencyStore.markProcessed(event.id());
    }
}

