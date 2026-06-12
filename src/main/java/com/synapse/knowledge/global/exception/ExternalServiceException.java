package com.synapse.knowledge.global.exception;

/**
 * 외부 서비스(예: Engagement 공유 검증 API) 호출이 실패했을 때 발생한다.
 * 502 Bad Gateway 로 응답한다.
 */
public class ExternalServiceException extends BusinessException {
    public ExternalServiceException(String message) {
        super(ErrorCode.EXTERNAL_API_ERROR, message);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(ErrorCode.EXTERNAL_API_ERROR, message, cause);
    }
}
