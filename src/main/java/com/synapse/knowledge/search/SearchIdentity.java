package com.synapse.knowledge.search;

public record SearchIdentity(
    Long userId,
    String semanticActorId
) {
    public SearchIdentity {
        if (userId == null) {
            throw new IllegalArgumentException("검색 사용자 식별자는 null일 수 없습니다");
        }
    }

    public boolean canUseSemanticSearch() {
        return semanticActorId != null && !semanticActorId.isBlank();
    }
}
