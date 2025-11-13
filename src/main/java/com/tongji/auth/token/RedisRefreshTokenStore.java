package com.tongji.auth.token;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;

/**
 * Redis-based refresh token whitelist storage.
 * <p>
 * Keyspace: `auth:rt:{userId}:{tokenId}`, value is always "1", TTL controls expiration.
 * Supports validating token validity, revoking individual tokens, or revoking all tokens for a user.
 */
@Component
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private final StringRedisTemplate redisTemplate;

    public RedisRefreshTokenStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Write refresh token to whitelist with expiration time.
     *
     * @param userId  User ID.
     * @param tokenId Refresh token ID.
     * @param ttl     Time to live (Redis TTL).
     */
    @Override
    public void storeToken(long userId, String tokenId, Duration ttl) {
        String key = key(userId, tokenId);
        redisTemplate.opsForValue().set(key, "1", ttl);
    }

    /**
     * Check if refresh token is still valid.
     *
     * @param userId  User ID.
     * @param tokenId Refresh token ID.
     * @return Whether valid (key exists and value is "1").
     */
    @Override
    public boolean isTokenValid(long userId, String tokenId) {
        String key = key(userId, tokenId);
        return Objects.equals("1", redisTemplate.opsForValue().get(key));
    }

    /**
     * Revoke a single refresh token.
     *
     * @param userId  User ID.
     * @param tokenId Refresh token ID.
     */
    @Override
    public void revokeToken(long userId, String tokenId) {
        redisTemplate.delete(key(userId, tokenId));
    }

    /**
     * Revoke all refresh tokens for this user.
     *
     * @param userId User ID.
     */
    @Override
    public void revokeAll(long userId) {
        String pattern = "auth:rt:%d:*".formatted(userId);
        var keys = redisTemplate.keys(pattern);
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * Generate whitelist key name.
     *
     * @param userId  User ID.
     * @param tokenId Refresh token ID.
     * @return Redis key name.
     */
    private static String key(long userId, String tokenId) {
        return "auth:rt:%d:%s".formatted(userId, tokenId);
    }
}
