package com.tongji.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class RedissonConfig {
    @Value("${counter.rebuild.lock.watchdog-ms:30000}")
    private long lockWatchdogMs;

    @Bean
    public RedissonClient redissonClient(RedisProperties redisProperties) {
        Config config = new Config();
        // Configure Redisson's lock watchdog timeout for automatic lock renewal
        config.setLockWatchdogTimeout(lockWatchdogMs);
        String address = "redis://" + redisProperties.getHost() + ":" + redisProperties.getPort();
        SingleServerConfig single = config.useSingleServer().setAddress(address);

        if (redisProperties.getPassword() != null && !redisProperties.getPassword().isEmpty()) {
            single.setPassword(redisProperties.getPassword());
        }

        // Spring Boot RedisProperties#getDatabase returns the original int (default 0), no null check needed
        single.setDatabase(redisProperties.getDatabase());
        return Redisson.create(config);
    }
}
