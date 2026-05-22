package com.synapse.knowledge.global.exception;

public class AccessDeniedException extends BusinessException {
    public AccessDeniedException(String message) {
        super(ErrorCode.ACCESS_DENIED, message);
    }
}
