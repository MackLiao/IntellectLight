package com.tongji.auth.verification;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * Redis-based verification code storage implementation.
 * <p>
 * Uses Hash structure to store `code`, `maxAttempts` and `attempts`, TTL controls expiration.
 * Supports attempt counting and error status return during verification, deletes key after success to prevent reuse.
 */
@Component
public class RedisVerificationCodeStore implements VerificationCodeStore {

    private static final String FIELD_CODE = "code";
    private static final String FIELD_MAX_ATTEMPTS = "maxAttempts";
    private static final String FIELD_ATTEMPTS = "attempts";

    private final StringRedisTemplate redisTemplate;

    public RedisVerificationCodeStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Save verification code to Redis Hash and set TTL.
     *
     * @param scene       Scene name.
     * @param identifier  Identifier (phone number or email).
     * @param code        Verification code string.
     * @param ttl         Expiration time.
     * @param maxAttempts Maximum attempt count.
     * @throws RedisSystemException thrown when save fails.
     */
    @Override
    public void saveCode(String scene, String identifier, String code, Duration ttl, int maxAttempts) {
        String key = buildKey(scene, identifier);
        HashOperations<String, String, String> ops = redisTemplate.opsForHash();
        try {
            ops.put(key, FIELD_CODE, code);
            ops.put(key, FIELD_MAX_ATTEMPTS, String.valueOf(maxAttempts));
            ops.put(key, FIELD_ATTEMPTS, "0");
            redisTemplate.expire(key, ttl);
        } catch (DataAccessException ex) {
            throw new RedisSystemException("Failed to save verification code", ex);
        }
    }

    /**
     * Verify if verification code matches, update attempt count and delete record on success.
     *
     * @param scene      Scene name.
     * @param identifier Identifier (phone number or email).
     * @param code       User input verification code.
     * @return Verification result (success, not found, error, too many attempts).
     */
    @Override
    public VerificationCheckResult verify(String scene, String identifier, String code) {
        String key = buildKey(scene, identifier);
        HashOperations<String, String, String> ops = redisTemplate.opsForHash();
        Map<String, String> data = ops.entries(key);
        if (data.isEmpty()) {
            return new VerificationCheckResult(VerificationCodeStatus.NOT_FOUND, 0, 0);
        }
        String storedCode = data.get(FIELD_CODE);
        int maxAttempts = parseInt(data.get(FIELD_MAX_ATTEMPTS), 5);
        int attempts = parseInt(data.get(FIELD_ATTEMPTS), 0);

        if (attempts >= maxAttempts) {
            return new VerificationCheckResult(VerificationCodeStatus.TOO_MANY_ATTEMPTS, attempts, maxAttempts);
        }

        if (Objects.equals(storedCode, code)) {
            redisTemplate.delete(key);
            return new VerificationCheckResult(VerificationCodeStatus.SUCCESS, attempts, maxAttempts);
        }

        int updatedAttempts = attempts + 1;
        ops.put(key, FIELD_ATTEMPTS, String.valueOf(updatedAttempts));
        if (updatedAttempts >= maxAttempts) {
            redisTemplate.expire(key, Duration.ofMinutes(30));
            return new VerificationCheckResult(VerificationCodeStatus.TOO_MANY_ATTEMPTS, updatedAttempts, maxAttempts);
        }
        return new VerificationCheckResult(VerificationCodeStatus.MISMATCH, updatedAttempts, maxAttempts);
    }

    /**
     * Invalidate verification code (delete storage record).
     *
     * @param scene      Scene name.
     * @param identifier Identifier (phone number or email).
     */
    @Override
    public void invalidate(String scene, String identifier) {
        redisTemplate.delete(buildKey(scene, identifier));
    }

    /**
     * Generate Redis key name for verification code.
     *
     * @param scene      Scene name.
     * @param identifier Identifier (phone number or email).
     * @return Key name string.
     */
    private static String buildKey(String scene, String identifier) {
        return "auth:code:%s:%s".formatted(scene, identifier);
    }

    /**
     * Parse integer string, return default value on failure.
     *
     * @param value        String to parse.
     * @param defaultValue Default value when parsing fails.
     * @return Integer value.
     */
    private static int parseInt(String value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
