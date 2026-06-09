package com.synapse.knowledge.e2e;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * E2E 테스트에서 인증을 통과시키기 위한 JWT 헬퍼.
 *
 * <p>실제 서명 토큰을 발급하는 대신 Spring Security Test의 {@code jwt()} post-processor로
 * {@code userId}/{@code subject} claim을 주입한다. 기존 controller 테스트(GraphControllerTest 등)와
 * 동일한 방식이며, 필터 체인 → 컨트롤러 → 실서비스 경로를 그대로 통과한다.
 */
final class E2eJwtHelper {

    private E2eJwtHelper() {
    }

    /** 숫자 userId claim 기반 인증 (knowledge-svc 기본 사용자 식별). */
    static RequestPostProcessor user(long userId) {
        return jwt().jwt(builder -> builder
            .subject("user-" + userId)
            .claim("userId", userId));
    }
}
