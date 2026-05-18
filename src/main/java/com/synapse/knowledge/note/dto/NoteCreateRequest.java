package com.synapse.knowledge.note.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoteCreateRequest(
    @NotBlank String tenantId,
    @NotBlank @Size(max = 200) String title,
    @NotBlank String contentMd
) {}
