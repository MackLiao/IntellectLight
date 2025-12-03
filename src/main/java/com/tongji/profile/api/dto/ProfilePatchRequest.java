package com.tongji.profile.api.dto;

import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Profile partial update request (PATCH).
 * <p>
 * Client only needs to submit fields to update; unsubmitted fields remain unchanged. Sending duplicate requests with same values is an idempotent operation.
 */
public record ProfilePatchRequest(
        @Size(min = 1, max = 64, message = "Nickname length must be between 1-64") String nickname,
        @Size(max = 512, message = "Bio length cannot exceed 512") String bio,
        @Pattern(regexp = "(?i)MALE|FEMALE|OTHER|UNKNOWN", message = "Gender must be MALE/FEMALE/OTHER/UNKNOWN") String gender,
        @PastOrPresent(message = "Birthday cannot be later than today") LocalDate birthday,
        @Pattern(regexp = "^[a-zA-Z0-9_]{4,32}$", message = "ZgId only supports letters, numbers, underscores, length 4-32") String zgId,
        @Size(max = 128, message = "School name length cannot exceed 128") String school,
        String tagJson
){ }
