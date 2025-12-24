package com.tongji.storage.api;

import com.tongji.common.exception.BusinessException;
import com.tongji.common.exception.ErrorCode;
import com.tongji.auth.token.JwtService;
import com.tongji.knowpost.mapper.KnowPostMapper;
import com.tongji.knowpost.model.KnowPost;
import com.tongji.storage.OssStorageService;
import com.tongji.storage.api.dto.StoragePresignRequest;
import com.tongji.storage.api.dto.StoragePresignResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/storage")
@Validated
@RequiredArgsConstructor
public class StorageController {

    private final OssStorageService ossStorageService;
    private final JwtService jwtService;
    private final KnowPostMapper knowPostMapper;

    /**
     * Get PUT presigned URL for direct upload.
     */
    @PostMapping("/presign")
    public StoragePresignResponse presign(@Valid @RequestBody StoragePresignRequest request,
                                          @AuthenticationPrincipal Jwt jwt) {
        long userId = jwtService.extractUserId(jwt);

        long postId;
        try {
            postId = Long.parseLong(request.postId());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Invalid postId");
        }

        // Permission check: postId must belong to current user
        KnowPost post = knowPostMapper.findById(postId);
        if (post == null || post.getCreatorId() == null || post.getCreatorId() != userId) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Draft does not exist or no permission");
        }

        String scene = request.scene();
        String objectKey;
        String ext = normalizeExt(request.ext(), request.contentType(), scene);

        if ("knowpost_content".equals(scene)) {
            objectKey = "posts/" + postId + "/content" + ext;
        } else if ("knowpost_image".equals(scene)) {
            String date = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.of("UTC")).format(Instant.now());
            String rand = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8);
            objectKey = "posts/" + postId + "/images/" + date + "/" + rand + ext;
        } else {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Unsupported upload scene");
        }

        int expiresIn = 600; // 10 minutes
        String putUrl = ossStorageService.generatePresignedPutUrl(objectKey, request.contentType(), expiresIn);
        Map<String, String> headers = Map.of("Content-Type", request.contentType());
        return new StoragePresignResponse(objectKey, putUrl, headers, expiresIn);
    }

    private String normalizeExt(String ext, String contentType, String scene) {
        if (ext != null && !ext.isBlank()) {
            return ext.startsWith(".") ? ext : "." + ext;
        }
        if ("knowpost_content".equals(scene)) {
            return switch (contentType) {
                case "text/markdown" -> ".md";
                case "text/html" -> ".html";
                case "text/plain" -> ".txt";
                case "application/json" -> ".json";
                default -> ".bin";
            };
        } else {
            return switch (contentType) {
                case "image/jpeg" -> ".jpg";
                case "image/png" -> ".png";
                case "image/webp" -> ".webp";
                default -> ".img";
            };
        }
    }
}
