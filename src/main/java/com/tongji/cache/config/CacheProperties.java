package com.tongji.cache.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cache related configuration items.
 *
 * <p>Configuration prefix: {@code cache}, used to bind cache parameters in {@code application.yml}.</p>
 */
@Component
@ConfigurationProperties(prefix = "cache")
@Data
public class CacheProperties {
    // In-process cache (local L2 cache) configuration.
    private L2 l2 = new L2();

    // Hot key identification and extension strategy configuration.
    private Hotkey hotkey = new Hotkey();

    @Data
    public static class L2 {
        // Public feed cache configuration.
        private PublicCfg publicCfg = new PublicCfg();

        // Personal feed cache configuration.
        private MineCfg mineCfg = new MineCfg();

        // Knowpost detail cache configuration
        private DetailCfg detailCfg = new DetailCfg();
    }

    @Data
    public static class PublicCfg {
        // TTL (seconds): duration to retain in local cache after write.
        private int ttlSeconds = 15;

        // Maximum entries: evict after exceeding based on Caffeine policy.
        private long maxSize = 1000;
    }

    @Data
    public static class MineCfg {
        // TTL (seconds): duration to retain in local cache after write.
        private int ttlSeconds = 10;

        // Maximum entries: evict after exceeding based on Caffeine policy.
        private long maxSize = 1000;
    }

    @Data
    public static class DetailCfg {
        // TTL (seconds): duration to retain in local cache after write.
        private int ttlSeconds = 30;

        // Maximum entries: evict after exceeding based on Caffeine policy.
        private long maxSize = 5000;
    }

    @Data
    public static class Hotkey {
        // Hot statistics window length (seconds).
        private int windowSeconds = 60;

        // Statistics window segment size (seconds), used for cumulative access count by segment.
        private int segmentSeconds = 10;

        // Low heat threshold: accesses within window reaching this value considered low heat.
        private int levelLow = 50;

        // Medium heat threshold: accesses within window reaching this value considered medium heat.
        private int levelMedium = 200;

        // High heat threshold: accesses within window reaching this value considered high heat.
        private int levelHigh = 500;

        // Low heat additional TTL extension (seconds).
        private int extendLowSeconds = 20;

        //Medium heat additional TTL extension (seconds).
        private int extendMediumSeconds = 60;

        // High heat additional TTL extension (seconds).
        private int extendHighSeconds = 120;
    }
}
