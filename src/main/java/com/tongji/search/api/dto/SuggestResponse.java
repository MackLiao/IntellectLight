package com.tongji.search.api.dto;

import java.util.List;

/**
 * Suggestion response: returns a list of candidate titles.
 */
public record SuggestResponse(
        List<String> items
) {}