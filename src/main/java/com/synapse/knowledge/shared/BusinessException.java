package com.synapse.knowledge.shared;

import lombok.Getter;

@Getter
public abstract class BusinessException extends RuntimeException {
    private final String errorCode;
    private final int status;

    protected BusinessException(String errorCode, int status, String message) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }
}
