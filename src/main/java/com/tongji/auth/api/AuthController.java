package com.tongji.auth.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.tongji.auth.api.dto.AuthResponse;
import com.tongji.auth.api.dto.AuthUserResponse;
import com.tongji.auth.api.dto.LoginRequest;
import com.tongji.auth.api.dto.LogoutRequest;
import com.tongji.auth.api.dto.PasswordResetRequest;
import com.tongji.auth.api.dto.RegisterRequest;
import com.tongji.auth.api.dto.SendCodeRequest;
import com.tongji.auth.api.dto.SendCodeResponse;
import com.tongji.auth.api.dto.TokenRefreshRequest;
import com.tongji.auth.api.dto.TokenResponse;
import com.tongji.auth.model.ClientInfo;
import com.tongji.auth.service.AuthService;
import com.tongji.auth.token.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication API controller.
 * <p>
 * Exposes REST endpoints: send verification code, register, login, refresh token, logout, reset password, and query current user info.
 * Integration: Uses Spring Security resource server capability; `/me` extracts the user via `@AuthenticationPrincipal Jwt`.
 * Client info: Parses IP and UA from request headers for login audit logging.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    /**
     * Send SMS/email verification code.
     * <p>
     * Sends a one-time verification code to the specified identifier (phone number or email) based on the scene (register, login, reset password).
     *
     * @param request Request body containing:
     *                - identifierType: identifier type, PHONE or EMAIL;
     *                - identifier: phone number or email address;
     *                - scene: verification code usage scene (REGISTER/LOGIN/RESET_PASSWORD).
     * @return Response body containing the target identifier, scene, and verification code expiration in seconds.
     */
    @PostMapping("/send-code")
    public SendCodeResponse sendCode(@Valid @RequestBody SendCodeRequest request) {
        return authService.sendCode(request);
    }

    /**
     * Register a new user and automatically log in.
     * <p>
     * Creates the user after verifying the identifier and verification code; if a password is provided, validates its complexity and saves the password hash; issues Access/Refresh Token upon success.
     *
     * @param request     Request body containing: identifier type and value, verification code, optional password, terms agreement flag.
     * @param httpRequest Used to resolve client info (IP and User-Agent) for audit logging.
     * @return Authentication response containing user info and token pair.
     */
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        return authService.register(request, resolveClient(httpRequest));
    }

    /**
     * Log in and obtain a token pair.
     * <p>
     * Supports two channels: password login or verification code login; issues Access/Refresh Token upon success.
     *
     * @param request     Request body containing: identifier type and value, password or verification code (one of the two).
     * @param httpRequest Used to resolve client info (IP and User-Agent) for audit logging.
     * @return Authentication response containing user info and token pair.
     */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return authService.login(request, resolveClient(httpRequest));
    }

    /**
     * Refresh tokens using a Refresh Token.
     * <p>
     * Validates the refresh token's legitimacy and allowlist status, issues a new token pair, and revokes the old refresh token.
     *
     * @param request Request body containing: refreshToken (the refresh token).
     * @return New token response (accessToken/refreshToken and their expiration times).
     */
    @PostMapping("/token/refresh")
    public TokenResponse refresh(@Valid @RequestBody TokenRefreshRequest request) {
        return authService.refresh(request);
    }

    /**
     * Log out and revoke the refresh token.
     * <p>
     * If the provided token is a valid Refresh Token, revokes its allowlist record; returns 204 with no response body.
     *
     * @param request Request body containing: refreshToken (the refresh token to revoke).
     * @return Empty response, HTTP 204 No Content.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    /**
     * Reset password using a verification code.
     * <p>
     * Updates the user's password hash after verifying the identifier and verification code, and revokes all of the user's refresh tokens to force logout.
     *
     * @param request Request body containing: identifier type and value, verification code, new password.
     * @return Empty response, HTTP 204 No Content.
     */
    @PostMapping("/password/reset")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Query current logged-in user info.
     * <p>
     * Extracts the user ID from the Spring Security injected `Jwt` token and returns the user summary.
     *
     * @param jwt The JWT token bound to the current request (from `Authorization: Bearer`).
     * @return User info response.
     */
    @GetMapping("/me")
    public AuthUserResponse me(@AuthenticationPrincipal Jwt jwt) {
        long userId = jwtService.extractUserId(jwt);
        return authService.me(userId);
    }

    /**
     * Resolve client info from the request.
     *
     * @param request HTTP request object.
     * @return Client info (IP and User-Agent).
     */
    private ClientInfo resolveClient(HttpServletRequest request) {
        String ip = extractClientIp(request);
        String ua = request.getHeader("User-Agent");
        return new ClientInfo(ip, ua);
    }

    /**
     * Extract the client IP address.
     * <p>
     * Prefers proxy headers: `X-Forwarded-For` (first entry), `X-Real-IP`; otherwise falls back to `request.getRemoteAddr()`.
     *
     * @param request HTTP request object.
     * @return Client IP.
     */
    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
