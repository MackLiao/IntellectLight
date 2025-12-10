package com.tongji.knowpost.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Content upload confirmation request.
 */
public record KnowPostContentConfirmRequest(
        @NotBlank String objectKey,
        @NotBlank String etag,
        @NotNull Long size,
        @NotBlank String sha256
) {}