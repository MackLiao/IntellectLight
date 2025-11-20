package com.tongji.auth.verification;

/**
 * Send verification code result.
 * <p>
 * Returns normalized account, sending scene, and verification code expiration time (seconds).
 */
public record SendCodeResult(String identifier,
                             VerificationScene scene,
                             int expireSeconds
) {
}
