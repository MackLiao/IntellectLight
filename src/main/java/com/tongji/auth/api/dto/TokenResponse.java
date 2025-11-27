package com.tongji.auth.api.dto;

import java.time.Instant;

/**
 * Token response.
 * <p>
 * Returns the access token and refresh token along with their expiration times, for client persistence and subsequent API calls.
 */
public record TokenResponse(
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt
) {
}
