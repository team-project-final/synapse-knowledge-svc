package com.synapse.knowledge.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.synapse.knowledge.global.exception.AuthenticationRequiredException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.context.request.ServletWebRequest;

class CurrentUserArgumentResolverTest {

    private final CurrentUserArgumentResolver resolver = new CurrentUserArgumentResolver();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("userId Claim이 숫자이고 sub가 UUID이면 현재 사용자를 반환한다")
    void resolveArgument_userIdClaimIsNumberAndSubIsUuid_shouldReturnCurrentUser() throws Exception {
        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .subject("11111111-1111-1111-1111-111111111111")
            .claim("userId", 10L)
            .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, AuthorityUtils.NO_AUTHORITIES));

        CurrentUser currentUser = (CurrentUser) resolver.resolveArgument(
            null,
            null,
            new ServletWebRequest(new MockHttpServletRequest()),
            null
        );

        assertThat(currentUser.userId()).isEqualTo(10L);
        assertThat(currentUser.subject()).isEqualTo("11111111-1111-1111-1111-111111111111");
    }

    @Test
    @DisplayName("userId Claim이 없으면 인증 예외를 던진다")
    void resolveArgument_noUserIdClaim_shouldThrowAuthenticationException() {
        Jwt jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .subject("22222222-2222-2222-2222-222222222222")
            .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, AuthorityUtils.NO_AUTHORITIES));

        assertThatThrownBy(() -> resolver.resolveArgument(
            null,
            null,
            new ServletWebRequest(new MockHttpServletRequest()),
            null
        )).isInstanceOf(AuthenticationRequiredException.class)
            .hasMessageContaining("토큰에서 userId를 확인할 수 없습니다");
    }
}
