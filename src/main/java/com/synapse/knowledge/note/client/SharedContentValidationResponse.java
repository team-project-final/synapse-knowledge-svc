package com.synapse.knowledge.note.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Engagement 공유 검증 API 응답.
 * 실패 시에도 예외가 아니라 {@code valid=false} 와 {@code reason} 으로 내려온다.
 * (reason: NOT_FOUND / TOKEN_MISMATCH / CONTENT_TYPE_MISMATCH / CONTENT_ID_MISMATCH)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SharedContentValidationResponse(
    boolean valid,
    String sharedContentId,
    String contentType,
    String contentId,
    String ownerId,
    String reason
) {
}
