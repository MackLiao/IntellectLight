package com.tongji.storage.api.dto;

import java.util.Map;

/**
 * Presigned direct upload response.
 */
public record StoragePresignResponse(
        String objectKey,
        String putUrl,
        Map<String, String> headers,
        int expiresIn
) {}