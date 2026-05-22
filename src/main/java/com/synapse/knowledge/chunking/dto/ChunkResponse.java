package com.synapse.knowledge.chunking.dto;

import com.synapse.knowledge.chunking.entity.NoteChunk;

public record ChunkResponse(
    Long id,
    Long noteId,
    Integer chunkIndex,
    String chunkText,
    Integer tokenCount
) {
    public static ChunkResponse from(NoteChunk noteChunk) {
        return new ChunkResponse(
            noteChunk.getId(),
            noteChunk.getNoteId(),
            noteChunk.getChunkIndex(),
            noteChunk.getChunkText(),
            noteChunk.getTokenCount()
        );
    }
}
