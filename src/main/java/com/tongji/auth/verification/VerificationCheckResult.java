package com.tongji.auth.verification;

/**
 * Verification code check result.
 * <p>
 * Contains status (success/not found/expired/error/too many attempts) and attempt count statistics, providing convenient success judgment.
 */
public record VerificationCheckResult(
        VerificationCodeStatus status,
        int attempts,
        int maxAttempts
) {
    public boolean isSuccess() {
        return status == VerificationCodeStatus.SUCCESS;
    }
}
