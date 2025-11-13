package com.tongji.auth.token;

import java.time.Instant;

/**
 * A pair of access token and refresh token.
 * <p>
 * Field description:
 * - accessToken: Access token (JWT string, used with Bearer);
 * - accessTokenExpiresAt: Access token expiration time;
 * - refreshToken: Refresh token (JWT string, used only for refresh endpoint);
 * - refreshTokenExpiresAt: Refresh token expiration time;
 * - refreshTokenId: Refresh token ID (jti, used for whitelist storage and revocation).
 */
public record TokenPair(
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt,
        String refreshTokenId
) {
}
