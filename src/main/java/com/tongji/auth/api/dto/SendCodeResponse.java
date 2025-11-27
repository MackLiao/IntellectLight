package com.tongji.auth.api.dto;

import com.tongji.auth.verification.VerificationScene;

/**
 * Send verification code response.
 * <p>
 * Returns the normalized account identifier, scene, and verification code validity period (in seconds).
 */
public record SendCodeResponse(
        String identifier,
        VerificationScene scene,
        int expireSeconds
) {
}
