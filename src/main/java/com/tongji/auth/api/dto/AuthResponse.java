package com.tongji.auth.api.dto;

/**
 * Authentication response.
 * <p>
 * Returned after successful login/registration: a combined result containing user info and token info.
 */
public record AuthResponse(
        AuthUserResponse user,
        TokenResponse token
) {
}
