package com.tongji.knowpost.api.dto;

import jakarta.validation.constraints.NotBlank;

public record DescriptionSuggestRequest(
        @NotBlank(message = "content must not be blank") String content
) {}