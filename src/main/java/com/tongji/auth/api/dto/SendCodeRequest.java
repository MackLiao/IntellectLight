package com.tongji.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.tongji.auth.model.IdentifierType;
import com.tongji.auth.verification.VerificationScene;

/**
 * Send verification code request.
 * <p>
 * `scene` specifies the scenario (register/login/reset password), combined with account type and value to generate and send a verification code.
 */
public record SendCodeRequest(
        @NotNull(message = "Scene is required") VerificationScene scene,
        @NotNull(message = "Account type is required") IdentifierType identifierType,
        @NotBlank(message = "Account identifier is required") String identifier
) {
}
