package com.tongji.auth.token;

import java.time.Duration;

/**
 * Refresh token whitelist storage interface.
 * <p>
 * Manages Refresh Token validity: storing, validating, revoking individual tokens, and revoking all user tokens.
 * Implementation can use Redis, database, or other persistence solutions.
 */
public interface RefreshTokenStore {

    /**
     * Store refresh token whitelist record.
     *
     * @param userId  User ID.
     * @param tokenId Refresh token ID (jti).
     * @param ttl     Time to live (automatically expires after expiration).
     */
    void storeToken(long userId, String tokenId, Duration ttl);

    /**
     * Validate whether refresh token is still valid (in whitelist and not expired).
     *
     * @param userId  User ID.
     * @param tokenId Refresh token ID (jti).
     * @return Whether valid.
     */
    boolean isTokenValid(long userId, String tokenId);

    /**
     * Revoke specified refresh token (remove from whitelist).
     *
     * @param userId  User ID.
     * @param tokenId Refresh token ID (jti).
     */
    void revokeToken(long userId, String tokenId);

    /**
     * Revoke all refresh tokens for a user (force all sessions of this user to logout).
     *
     * @param userId User ID.
     */
    void revokeAll(long userId);
}
