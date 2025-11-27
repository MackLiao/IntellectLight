package com.tongji.auth.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Token refresh request.
 * <p>
 * Provides the old refresh token; after server validation, a new access/refresh token pair is returned.
 */
public record TokenRefreshRequest(@NotBlank(message = "Refresh token is required") String refreshToken) {
}
