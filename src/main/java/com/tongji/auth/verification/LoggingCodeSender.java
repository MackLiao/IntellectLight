package com.tongji.auth.verification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Development/testing verification code sender.
 * <p>
 * Does not actually send, only logs, convenient for local development and integration testing.
 */
@Slf4j
@Component
public class LoggingCodeSender implements CodeSender {

    /**
     * Log verification code sending info (does not actually send).
     *
     * @param scene         Verification code scene.
     * @param identifier    Identifier (phone number or email).
     * @param code          Verification code content.
     * @param expireMinutes Expiration time (minutes).
     */
    @Override
    public void sendCode(VerificationScene scene, String identifier, String code, int expireMinutes) {
        log.info("Send verification code scene={} identifier={} code={} expireMinutes={}", scene, identifier, code, expireMinutes);
    }
}
