package com.synapse.knowledge.global.exception;

public enum ErrorCode {
    COMMON_BAD_REQUEST("C400", 400),
    AUTHENTICATION_REQUIRED("C401", 401),
    ACCESS_DENIED("C403", 403),
    NOTE_NOT_FOUND("KNOW-404", 404),
    INTERNAL_ERROR("C500", 500);

    private final String code;
    private final int status;

    ErrorCode(String code, int status) {
        this.code = code;
        this.status = status;
    }

    public String code() {
        return code;
    }

    public int status() {
        return status;
    }
}
