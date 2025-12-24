package com.tongji.storage.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Presigned direct upload request.
 */
public record StoragePresignRequest(
        @NotBlank String scene, // knowpost_content | knowpost_image
        @NotBlank String postId, // String to avoid precision loss on frontend
        @NotBlank String contentType,
        String ext
) {}