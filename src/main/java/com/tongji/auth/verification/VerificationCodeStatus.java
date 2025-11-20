package com.tongji.auth.verification;

// Corresponding to: success, not found, expired, mismatch, too many attempts
public enum VerificationCodeStatus {
    SUCCESS,
    NOT_FOUND,
    EXPIRED,
    MISMATCH,
    TOO_MANY_ATTEMPTS
}
