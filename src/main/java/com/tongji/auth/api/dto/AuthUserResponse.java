package com.tongji.auth.api.dto;

import java.time.LocalDate;

/**
 * Authenticated user response.
 * <p>
 * Basic user info exposed to the client, used for “who am I” and homepage display.
 */
public record AuthUserResponse(
        Long id,
        String nickname,
        String avatar,
        String phone,
        String zhId,
        LocalDate birthday,
        String school,
        String bio,
        String gender,
        String tagJson
) {
}
