package com.synapse.knowledge.global.exception;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(OutputCaptureExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("예상하지 못한 예외는 500 응답과 error 로그를 남긴다")
    void handleException_unexpectedException_shouldReturn500AndLogError(CapturedOutput output) {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getRequestURI()).thenReturn("/api/v1/notes/search");

        RuntimeException ex = new RuntimeException("boom");

        ResponseEntity<ErrorResponse> response = handler.handleException(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.INTERNAL_ERROR.code());
        assertThat(output.getAll()).contains("Unhandled exception occurred path=/api/v1/notes/search");
        assertThat(output.getAll()).contains("java.lang.RuntimeException: boom");
    }
}
