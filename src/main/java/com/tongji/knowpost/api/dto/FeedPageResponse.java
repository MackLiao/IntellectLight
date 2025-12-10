package com.tongji.knowpost.api.dto;

import java.util.List;

/**
 * Paginated response for the homepage feed.
 */
public record FeedPageResponse(
        List<FeedItemResponse> items,
        int page,
        int size,
        boolean hasMore
) {}