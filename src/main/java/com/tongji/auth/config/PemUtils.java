package com.tongji.auth.config;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * PEM key reading utility.
 * <p>
 * Supports reading PKCS#8 private keys and X.509 public keys from `Resource`, stripping headers/footers and whitespace before Base64 decoding,
 * producing `RSAPrivateKey` and `RSAPublicKey`. Used for JWT RS256 encoding/decoding configuration.
 */
public final class PemUtils {

    private static final String PRIVATE_BEGIN = "-----BEGIN PRIVATE KEY-----";
    private static final String PRIVATE_END = "-----END PRIVATE KEY-----";
    private static final String PUBLIC_BEGIN = "-----BEGIN PUBLIC KEY-----";
    private static final String PUBLIC_END = "-----END PUBLIC KEY-----";

    private PemUtils() {
    }

    /**
     * Read RSA private key (PKCS#8 format) from PEM resource.
     *
     * @param resource Spring {@link org.springframework.core.io.Resource} pointing to the private key PEM file.
     * @return Parsed {@link RSAPrivateKey}.
     * @throws IllegalStateException when reading or parsing fails.
     */
    public static RSAPrivateKey readPrivateKey(Resource resource) {
        try {
            String pem = readResource(resource);
            String keyData = pem.replace(PRIVATE_BEGIN, "")
                    .replace(PRIVATE_END, "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(keyData);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) kf.generatePrivate(spec);
        } catch (IOException | GeneralSecurityException ex) {
            throw new IllegalStateException("Failed to read RSA private key", ex);
        }
    }

    /**
     * Read RSA public key (X.509 format) from PEM resource.
     *
     * @param resource Spring {@link org.springframework.core.io.Resource} pointing to the public key PEM file.
     * @return Parsed {@link RSAPublicKey}.
     * @throws IllegalStateException when reading or parsing fails.
     */
    public static RSAPublicKey readPublicKey(Resource resource) {
        try {
            String pem = readResource(resource);
            String keyData = pem.replace(PUBLIC_BEGIN, "")
                    .replace(PUBLIC_END, "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(keyData);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) kf.generatePublic(spec);
        } catch (IOException | GeneralSecurityException ex) {
            throw new IllegalStateException("Failed to read RSA public key", ex);
        }
    }

    /**
     * Read text content from the given resource.
     *
     * @param resource Resource to read.
     * @return Text content decoded using UTF-8.
     * @throws IOException when I/O error occurs.
     */
    private static String readResource(Resource resource) throws IOException {
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
