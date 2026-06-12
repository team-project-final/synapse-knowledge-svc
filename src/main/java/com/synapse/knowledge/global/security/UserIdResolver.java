package com.synapse.knowledge.global.security;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * 외부 식별자(JWT subject, Engagement ownerId 등)를 내부 Long userId로 변환한다.
 * 숫자면 그대로, 아니면(UUID 등) {@code nameUUIDFromBytes} 기반 결정적 해시를 쓴다.
 * 동일 사용자가 서비스 전반에서 같은 Long userId를 갖도록 타 서비스와 동일 알고리즘을 유지한다.
 */
public final class UserIdResolver {

    private UserIdResolver() {
    }

    public static Long resolve(String subject) {
        try {
            return Long.valueOf(subject);
        } catch (NumberFormatException ex) {
            UUID uuid = UUID.nameUUIDFromBytes(subject.getBytes(StandardCharsets.UTF_8));
            return uuid.getMostSignificantBits() & Long.MAX_VALUE;
        }
    }
}
