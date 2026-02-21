package com.tongji.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

import java.time.Duration;

/**
 * Authentication configuration properties, bound to the prefix {@code auth.*}.
 *
 * <p>Contains the following groups:</p>
 * - Jwt: token issuance and verification configuration;
 * - Verification: verification code sending and validation configuration;
 * - Password: password policy and encryption strength configuration.
 */
@Data
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    /** JWT configuration. */
    private final Jwt jwt = new Jwt();
    /** Verification code configuration. */
    private final Verification verification = new Verification();
    /** Password policy configuration. */
    private final Password password = new Password();

    @Data
    public static class Jwt {
        /** JWT issuer identifier (iss). */
        private String issuer = "intellectlight";
        /** Access token time-to-live (TTL). */
        private Duration accessTokenTtl = Duration.ofMinutes(15);
        /** Refresh token time-to-live (TTL). */
        private Duration refreshTokenTtl = Duration.ofDays(7);
        /** JWK key identifier (kid), used for downstream verification and rotation. */
        private String keyId = "intellectlight-key";
        /** RSA private key PEM (PKCS#8) resource. */
        private Resource privateKey;
        /** RSA public key PEM (X.509) resource. */
        private Resource publicKey;
    }

    /**
     * Verification code configuration: digit count, validity period, max attempts, send interval, and daily limit.
     */
    @Data
    public static class Verification {
        /** Number of digits in the verification code. */
        private int codeLength = 6;
        /** Verification code validity period. */
        private Duration ttl = Duration.ofMinutes(5);
        /** Maximum verification attempt count. */
        private int maxAttempts = 5;
        /** Minimum interval between consecutive sends for the same identifier. */
        private Duration sendInterval = Duration.ofSeconds(60);
        /** Daily send limit per identifier. */
        private int dailyLimit = 10;
    }

    /** Password policy configuration. */
    @Data
    public static class Password {
        /** Password hash strength (BCrypt cost). */
        private int bcryptStrength = 12;
        /** Minimum password length. */
        private int minLength = 8;
    }
}
