package com.synapse.knowledge.chunking.service.listener;

import com.synapse.knowledge.chunking.service.ChunkingService;
import com.synapse.knowledge.shared.NoteChunkingRequested;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
class ChunkingEventListener {
    private final ChunkingService chunkingService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NoteChunkingRequested event) {
        chunkingService.chunkNote(event.noteId(), event.actorId(), event.contentPlain());
    }
}
