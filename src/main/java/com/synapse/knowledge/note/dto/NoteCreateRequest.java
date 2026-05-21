package com.synapse.knowledge.note.dto;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoteCreateRequest(
    @NotBlank String tenantId,
    @NotBlank @Size(max = 200) String title,
    @NotBlank String contentMd,
    @Size(max = 10) List<@NotBlank @Size(max = 30) String> tags
) {
    public NoteCreateRequest(String tenantId, String title, String contentMd) {
        this(tenantId, title, contentMd, List.of());
    }
}
