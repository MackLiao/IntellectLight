package com.tongji.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.tongji.auth.model.IdentifierType;

/**
 * Password reset request.
 * <p>
 * Sets a new password after verifying identity via verification code. The password must meet complexity policy.
 */
public record PasswordResetRequest(
        @NotNull(message = "Account type is required") IdentifierType identifierType,
        @NotBlank(message = "Account identifier is required") String identifier,
        @NotBlank(message = "Verification code is required") String code,
        @NotBlank(message = "New password is required") String newPassword
) {
}
