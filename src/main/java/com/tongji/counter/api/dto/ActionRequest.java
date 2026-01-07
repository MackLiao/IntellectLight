package com.tongji.counter.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Action request body: entity identifier for like/favorite operations.
 */
@Data
public class ActionRequest {
    @NotBlank
    private String entityType; // e.g. knowpost
    @NotBlank
    private String entityId;   // Content ID
}