package com.synapse.knowledge.note.dto;

/**
 * 노트 공유 가능 여부 확인 시, 공유 불가 사유.
 * 응답에는 {@code name()} 값(예: "NOT_OWNER")이 그대로 내려간다.
 */
public enum ShareableReason {
    NOT_FOUND,
    NOT_OWNER,
    NOT_ACTIVE
}
