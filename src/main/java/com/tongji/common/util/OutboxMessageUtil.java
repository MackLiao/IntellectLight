package com.tongji.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Outbox message parsing utility.
 *
 * <p>Used to consume Canal pushed binlog JSON messages and extract row data from outbox table (INSERT/UPDATE).</p>
 */
public final class OutboxMessageUtil {
    /**
     * Utility class, instantiation not allowed.
     */
    private OutboxMessageUtil() {}

    /**
     * Extract changed rows from outbox table in Canal message.
     *
     * <p>Only handles:</p>
     * <ul>
     *   <li>table = outbox</li>
     *   <li>type ∈ {INSERT, UPDATE}</li>
     *   <li>data is an array (each element is a column set of a row)</li>
     * </ul>
     *
     * @param objectMapper Jackson parser
     * @param message Canal JSON message
     * @return outbox row array; returns empty list if no match or parse failure
     */
    public static List<JsonNode> extractRows(ObjectMapper objectMapper, String message) {
        try {
            JsonNode root = objectMapper.readTree(message);

            JsonNode table = root.get("table");
            if (table == null || !"outbox".equals(table.asText())) {
                return Collections.emptyList();
            }

            JsonNode type = root.get("type");
            if (type == null || (!"INSERT".equals(type.asText()) && !"UPDATE".equals(type.asText()))) {
                return Collections.emptyList();
            }

            JsonNode data = root.get("data");
            if (data == null || !data.isArray()) {
                return Collections.emptyList();
            }
            List<JsonNode> rows = new ArrayList<>();
            data.forEach(rows::add);
            return rows;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
