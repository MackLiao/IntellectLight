package com.tongji.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security configuration.
 * <p>
 * - CSRF disabled (pure API backend, using JWT without session);
 * - CORS enabled, currently allows all origins (whitelist to be configured later);
 * - Stateless session;
 * - Public authentication endpoints and health check, other endpoints require authentication;
 * - Resource server enables JWT validation.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Configures Spring Security filter chain.
     *
     * <p>Mainly includes:</p>
     * - CSRF disabled;
     * - CORS enabled;
     * - Stateless session policy;
     * - Public auth endpoints and health check, other endpoints require authentication;
     * - Resource server JWT validation enabled.
     *
     * @param http Spring's {@link HttpSecurity} builder.
     * @return Built {@link SecurityFilterChain}.
     * @throws Exception exceptions that may be thrown during filter chain building.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // Public content: homepage Feed does not require login
                        .requestMatchers("/api/v1/knowposts/feed").permitAll()
                        // KnowPost details (public published content, non-public validated by service layer)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/knowposts/detail/*").permitAll()
                        // KnowPost detail page RAG Q&A (SSE streaming) allows anonymous access
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/knowposts/*/qa/stream").permitAll()
                        .requestMatchers(
                                "/api/v1/auth/send-code",
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/api/v1/auth/token/refresh",
                                "/api/v1/auth/logout",
                                "/api/v1/auth/password/reset"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()));
        return http.build();
    }

    /**
     * Defines and provides CORS configuration source.
     *
     * <p>Currently allows all origins (recommend replacing with product whitelist), allows common methods and headers, and does not allow credentials.</p>
     *
     * @return {@link CorsConfigurationSource}, used to register CORS rules for all paths.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*")); // TODO replace with product whitelist
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
