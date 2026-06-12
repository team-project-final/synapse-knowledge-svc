package com.synapse.knowledge.note.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * 공유 경로 기반 노트 복사 요청 본문.
 * sharedContentId/shareToken 은 Engagement 가 발급한 공유 접근 값이다.
 */
public record CopyFromShareRequest(
    @NotNull UUID sharedContentId,
    @NotBlank String shareToken
) {
}
