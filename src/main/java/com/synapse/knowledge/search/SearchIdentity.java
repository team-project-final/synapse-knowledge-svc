package com.synapse.knowledge.search;

public record SearchIdentity(
    Long userId,
    String semanticTenantId,
    String accessToken
) {
    public SearchIdentity {
        if (userId == null) {
            throw new IllegalArgumentException("검색 사용자 식별자는 null일 수 없습니다");
        }
    }

    public static SearchIdentity forRuntime(Long userId, String accessToken) {
        return new SearchIdentity(userId, null, accessToken);
    }

    public static SearchIdentity forBenchmark(Long userId, String semanticTenantId) {
        return new SearchIdentity(userId, semanticTenantId, null);
    }

    public boolean canUseSemanticSearch() {
        return hasSemanticTenantId() || hasAccessToken();
    }

    public boolean hasSemanticTenantId() {
        return semanticTenantId != null && !semanticTenantId.isBlank();
    }

    public boolean hasAccessToken() {
        return accessToken != null && !accessToken.isBlank();
    }
}
