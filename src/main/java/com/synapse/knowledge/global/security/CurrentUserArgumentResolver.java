package com.synapse.knowledge.global.security;

import com.synapse.knowledge.global.exception.AuthenticationRequiredException;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserAuth.class)
            && CurrentUser.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
        MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationRequiredException("인증이 필요합니다");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Jwt jwt)) {
            throw new AuthenticationRequiredException("유효한 JWT principal이 필요합니다");
        }

        Long userId = extractRequiredUserIdClaim(jwt, webRequest.getNativeRequest(HttpServletRequest.class));

        return new CurrentUser(userId, jwt.getSubject());
    }

    private Long extractRequiredUserIdClaim(Jwt jwt, HttpServletRequest request) {
        Object userIdClaim = jwt.getClaim("userId");
        if (userIdClaim instanceof Number number) {
            return number.longValue();
        }
        if (userIdClaim instanceof String text && !text.isBlank()) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ignored) {
                // fall through to subject-based resolution
            }
        }
        // 발급자(platform)는 숫자 userId claim 없이 subject에 UUID를 넣는다.
        // 숫자 userId가 없으면 subject를 결정적 Long으로 도출한다(engagement 등 타 서비스와 동일 알고리즘).
        String subject = jwt.getSubject();
        if (subject != null && !subject.isBlank()) {
            return resolveUserId(subject);
        }
        String requestUri = request == null ? "unknown" : request.getRequestURI();
        log.warn("JWT has no numeric userId claim and no subject. uri={}", requestUri);
        throw new AuthenticationRequiredException("토큰에서 userId를 확인할 수 없습니다");
    }

    /**
     * 외부 userId(JWT subject 등)를 내부 Long userId로 변환한다.
     * 숫자면 그대로, 아니면(UUID 등) nameUUIDFromBytes 기반 결정적 해시 —
     * 동일 사용자가 서비스 전반에서 같은 Long을 갖도록 타 서비스와 동일 알고리즘을 쓴다.
     */
    private static Long resolveUserId(String subject) {
        try {
            return Long.valueOf(subject);
        } catch (NumberFormatException ex) {
            UUID uuid = UUID.nameUUIDFromBytes(subject.getBytes(StandardCharsets.UTF_8));
            return uuid.getMostSignificantBits() & Long.MAX_VALUE;
        }
    }
}
