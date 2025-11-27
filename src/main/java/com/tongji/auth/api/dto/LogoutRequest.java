package com.tongji.auth.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Logout request.
 * <p>
 * Provides the refresh token to revoke the corresponding session, ensuring the token can no longer be used.
 */
public record LogoutRequest(@NotBlank(message = "Refresh token is required") String refreshToken) {
}
