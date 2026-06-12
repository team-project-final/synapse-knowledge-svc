package com.synapse.knowledge.note.dto;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record NoteCreateRequest(
    @NotBlank String tenantId,
    @NotBlank @Size(max = 200) String title,
    @NotBlank String contentMd,
    @Size(max = 10) List<@NotBlank @Size(max = 30) String> tags,
    @Pattern(
        regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
        message = "deckId는 UUID 형식이어야 합니다"
    ) String deckId
) {
    public NoteCreateRequest(String tenantId, String title, String contentMd) {
        this(tenantId, title, contentMd, List.of(), null);
    }

    public NoteCreateRequest(String tenantId, String title, String contentMd, List<String> tags) {
        this(tenantId, title, contentMd, tags, null);
    }
}
