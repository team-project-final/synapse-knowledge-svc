package com.synapse.knowledge.note.dto;

import com.synapse.knowledge.note.entity.NoteVersion;
import java.time.LocalDateTime;

public record NoteVersionDetailResponse(
    Long id,
    Integer versionNo,
    String title,
    String contentMd,
    LocalDateTime createdAt
) {
    public static NoteVersionDetailResponse from(NoteVersion v) {
        return new NoteVersionDetailResponse(v.getId(), v.getVersionNo(), v.getTitle(), v.getContentMd(), v.getCreatedAt());
    }
}
