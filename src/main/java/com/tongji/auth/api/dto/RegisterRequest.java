package com.tongji.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.tongji.auth.model.IdentifierType;

/**
 * Registration request.
 * <p>
 * Fields: account type and value, verification code, optional password, terms of service agreement flag.
 * Validation: must pass verification code check; when a password is provided, it must pass password policy validation.
 */
public record RegisterRequest(
        @NotNull(message = "Account type is required") IdentifierType identifierType,
        @NotBlank(message = "Account identifier is required") String identifier,
        @NotBlank(message = "Verification code is required") String code,
        String password,
        boolean agreeTerms
) {
}
