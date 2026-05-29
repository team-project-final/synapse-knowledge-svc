package com.synapse.knowledge.search.service.listener;

import com.synapse.knowledge.search.entity.NoteSearchDocument;
import com.synapse.knowledge.search.repository.NoteSearchRepository;
import com.synapse.knowledge.shared.NoteSearchSyncRequested;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NoteSearchIndexingListener {

    private final NoteSearchRepository noteSearchRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNoteSearchSyncRequested(NoteSearchSyncRequested event) {
        if (event.deleted()) {
            noteSearchRepository.deleteByNoteId(event.noteId());
            return;
        }

        noteSearchRepository.upsert(
            new NoteSearchDocument(
                event.noteId(),
                event.externalNoteId(),
                event.tenantId(),
                event.userId(),
                event.title(),
                event.contentPlain(),
                event.tags() == null ? List.of() : event.tags(),
                Instant.now()
            )
        );
    }
}
