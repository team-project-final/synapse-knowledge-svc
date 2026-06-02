package com.synapse.knowledge.chunking;

import com.synapse.knowledge.chunking.dto.ChunkResponse;
import java.util.List;

/**
 * Chunking 모듈의 공개 진입점이다.
 */
public interface ChunkingModuleApi {
    void chunkNote(Long noteId, String tenantId, String contentPlain);

    List<ChunkResponse> getNoteChunksByNoteId(Long noteId);

    void deleteNoteChunksByNoteId(Long noteId);
}
