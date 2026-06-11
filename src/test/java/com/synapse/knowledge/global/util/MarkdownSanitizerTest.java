package com.synapse.knowledge.global.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MarkdownSanitizerTest {

    private final MarkdownSanitizer markdownSanitizer = new MarkdownSanitizer();

    @Test
    @DisplayName("sanitize_null입력_shouldNull반환")
    void sanitize_null입력_shouldNull반환() {
        assertThat(markdownSanitizer.sanitize(null)).isNull();
    }

    @Test
    @DisplayName("sanitize_스크립트포함마크다운_should위험태그를제거")
    void sanitize_스크립트포함마크다운_should위험태그를제거() {
        String rawMarkdown = """
            정상 문단
            <script>alert('xss')</script>
            <a href="javascript:alert('xss')">unsafe</a>
            <b>safe</b>
            """;

        String sanitized = markdownSanitizer.sanitize(rawMarkdown);

        assertThat(sanitized).contains("정상 문단", "<b>safe</b>", "unsafe");
        assertThat(sanitized).doesNotContain("<script>", "javascript:alert");
    }
}
