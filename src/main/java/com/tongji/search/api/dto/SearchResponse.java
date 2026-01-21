package com.tongji.search.api.dto;

import com.tongji.knowpost.api.dto.FeedItemResponse;
import java.util.List;

/**
 * Search response: contains the result list and pagination cursor.
 */
public record SearchResponse(
        List<FeedItemResponse> items,
        String nextAfter,
        boolean hasMore
) {}