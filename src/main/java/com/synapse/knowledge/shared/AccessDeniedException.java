package com.synapse.knowledge.shared;

public class AccessDeniedException extends BusinessException {
    public AccessDeniedException(String message) {
        super("ACCESS_DENIED", 403, message);
    }
}
