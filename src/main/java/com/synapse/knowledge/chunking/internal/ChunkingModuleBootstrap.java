package com.synapse.knowledge.chunking.internal;

import com.synapse.knowledge.chunking.ChunkingModuleApi;
import com.synapse.knowledge.chunking.application.ChunkingService;
import com.synapse.knowledge.chunking.dto.ChunkResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ChunkingModuleBootstrap implements ChunkingModuleApi {
    private final ChunkingService chunkingService;

    @Override
    public void chunkNote(Long noteId, String tenantId, String contentPlain) {
        chunkingService.chunkNote(noteId, tenantId, contentPlain);
    }

    @Override
    public List<ChunkResponse> getNoteChunksByNoteId(Long noteId) {
        return chunkingService.getNoteChunksByNoteId(noteId);
    }

    @Override
    public void deleteNoteChunksByNoteId(Long noteId) {
        chunkingService.deleteNoteChunksByNoteId(noteId);
    }
}
