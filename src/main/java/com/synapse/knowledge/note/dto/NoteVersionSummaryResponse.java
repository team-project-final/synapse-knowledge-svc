package com.synapse.knowledge.note.dto;

import com.synapse.knowledge.note.entity.NoteVersion;
import java.time.LocalDateTime;

public record NoteVersionSummaryResponse(
    Long id,
    Integer versionNo,
    String title,
    LocalDateTime createdAt
) {
    public static NoteVersionSummaryResponse from(NoteVersion v) {
        return new NoteVersionSummaryResponse(v.getId(), v.getVersionNo(), v.getTitle(), v.getCreatedAt());
    }
}
