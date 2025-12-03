package com.tongji.profile.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // Allow any origin; change to specific domain if limiting
        config.setAllowedOriginPatterns(List.of("*"));
        // Allow common cross-origin methods including preflight
        config.setAllowedMethods(List.of("PATCH", "POST", "GET", "OPTIONS"));
        // Allow all headers including Authorization, Content-Type, etc.
        config.setAllowedHeaders(List.of("*"));
        // Do not use cross-origin credentials (set to true and limit specific origin if frontend needs Cookie)
        config.setAllowCredentials(false);
        // Preflight cache time
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Only enable CORS for Profile related endpoints
        source.registerCorsConfiguration("/api/v1/profile/**", config);
        return new CorsFilter(source);
    }
}
