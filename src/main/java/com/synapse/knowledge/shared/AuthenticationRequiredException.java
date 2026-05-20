package com.synapse.knowledge.shared;

public class AuthenticationRequiredException extends BusinessException {
    public AuthenticationRequiredException(String message) {
        super("KNOW-401", 401, message);
    }
}
