package com.synapse.knowledge.chunking.dto;

import com.synapse.knowledge.chunking.domain.NoteChunk;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChunkMapper {
    ChunkResponse toResponse(NoteChunk noteChunk);

    List<ChunkResponse> toResponses(List<NoteChunk> noteChunks);
}
