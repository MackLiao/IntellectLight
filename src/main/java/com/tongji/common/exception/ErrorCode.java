package com.tongji.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    IDENTIFIER_EXISTS("IDENTIFIER_EXISTS", "Account already exists"),
    IDENTIFIER_NOT_FOUND("IDENTIFIER_NOT_FOUND", "Account not found"),
    ZGID_EXISTS("ZGID_EXISTS", "Zhiguang ID already exists"),
    VERIFICATION_RATE_LIMIT("VERIFICATION_RATE_LIMIT", "Verification code sent too frequently"),
    VERIFICATION_DAILY_LIMIT("VERIFICATION_DAILY_LIMIT", "Verification code send limit exceeded"),
    VERIFICATION_NOT_FOUND("VERIFICATION_NOT_FOUND", "Verification code not found or expired"),
    VERIFICATION_MISMATCH("VERIFICATION_MISMATCH", "Incorrect verification code"),
    VERIFICATION_TOO_MANY_ATTEMPTS("VERIFICATION_TOO_MANY_ATTEMPTS", "Too many verification code attempts"),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "Invalid login credentials"),
    PASSWORD_POLICY_VIOLATION("PASSWORD_POLICY_VIOLATION", "Password strength insufficient"),
    TERMS_NOT_ACCEPTED("TERMS_NOT_ACCEPTED", "Please accept terms of service first"),
    REFRESH_TOKEN_INVALID("REFRESH_TOKEN_INVALID", "Refresh token invalid"),
    BAD_REQUEST("BAD_REQUEST", "Invalid request parameter"),
    INTERNAL_ERROR("INTERNAL_ERROR", "Internal server error");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}
