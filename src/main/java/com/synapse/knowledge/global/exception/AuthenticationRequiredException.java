package com.synapse.knowledge.global.exception;

public class AuthenticationRequiredException extends BusinessException {
    public AuthenticationRequiredException(String message) {
        super(ErrorCode.AUTHENTICATION_REQUIRED, message);
    }
}
