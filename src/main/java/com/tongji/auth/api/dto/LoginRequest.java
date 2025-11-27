package com.tongji.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.tongji.auth.model.IdentifierType;

/**
 * Login request.
 * <p>
 * Supports two channels:
 * - Verification code login: provide `code`;
 * - Password login: provide `password` (when the user has set one).
 * `identifierType` specifies the account type (phone/email), `identifier` is the account value.
 */
public record LoginRequest(
        @NotNull(message = "Account type is required") IdentifierType identifierType,
        @NotBlank(message = "Account identifier is required") String identifier,
        String code,
        String password
) {
}
