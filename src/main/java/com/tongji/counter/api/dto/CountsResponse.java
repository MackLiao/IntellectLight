package com.tongji.counter.api.dto;

import lombok.Data;

import java.util.Map;

/**
 * Counter response body: returns entity type, ID, and count values for each metric.
 */
@Data
public class CountsResponse {
    private String entityType;
    private String entityId;
    private Map<String, Long> counts;

    /**
     * Construct the response.
     * @param entityType entity type
     * @param entityId entity ID
     * @param counts mapping from metric name to count value
     */
    public CountsResponse(String entityType, String entityId, Map<String, Long> counts) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.counts = counts;
    }
}