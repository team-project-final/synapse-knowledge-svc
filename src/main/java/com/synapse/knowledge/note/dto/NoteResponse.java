package com.synapse.knowledge.note.dto;

import com.synapse.knowledge.note.domain.Note;
import java.time.LocalDateTime;

public record NoteResponse(
    Long id, 
    String title, 
    String contentMd, 
    String contentPlain,
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
            note.getStatus(), 
            note.getCreatedAt(),
            note.getUpdatedAt()
        );
    }
}
