package com.synapse.knowledge.note.dto;

import com.synapse.knowledge.note.entity.Note;
import java.time.LocalDateTime;
import java.util.List;

public record NoteResponse(
    Long id, 
    String title, 
    String contentMd, 
    String contentPlain,
    List<String> tags,
    String deckId,
    String status, 
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static NoteResponse from(Note note) {
        return new NoteResponse(
            note.getId(), 
            note.getTitle(), 
            note.getContentMd(), 
            note.getContentPlain(),
            note.getTags(),
            note.getDeckId(),
            note.getStatus(), 
            note.getCreatedAt(),
            note.getUpdatedAt()
        );
    }
}
