package com.synapse.knowledge.note.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Engagement 서비스 직호출 설정. 공유 노트 상세/복사 시 공유 경로 유효성 검증에 사용한다.
 */
@Validated
@ConfigurationProperties(prefix = "engagement")
public record EngagementProperties(
    @NotBlank String baseUrl,
    @NotNull Duration timeout
) {
}
