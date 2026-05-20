package com.synapse.knowledge.shared;

public class AccessDeniedException extends BusinessException {
    public AccessDeniedException(String message) {
        super("KNOW-403", 403, message);
    }
}
