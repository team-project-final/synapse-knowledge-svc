package com.synapse.knowledge.global.security;

import com.synapse.knowledge.global.exception.AuthenticationRequiredException;
import jakarta.servlet.http.HttpServletRequest;
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
                // handled below with a consistent error
            }
        }
        String requestUri = request == null ? "unknown" : request.getRequestURI();
        log.warn("JWT userId claim is missing or not numeric. subject={}, uri={}", jwt.getSubject(), requestUri);
        throw new AuthenticationRequiredException("토큰에서 userId를 확인할 수 없습니다");
    }
}
