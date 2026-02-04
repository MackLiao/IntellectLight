package com.tongji.search.outbox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tongji.relation.outbox.OutboxTopics;
import com.tongji.common.util.OutboxMessageUtil;
import com.tongji.search.index.SearchIndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Outbox consumer for the search index: listens to canal-outbox and drives incremental ES index updates.
 * Only processes upsert and soft-delete for entity=knowpost.
 */
@Service
@RequiredArgsConstructor
public class CanalOutboxConsumerSearch {
    private final ObjectMapper objectMapper;
    private final SearchIndexService indexService;

    /**
     * Consumes outbox messages, parses valid rows, and updates the index by entity type.
     */
    @KafkaListener(topics = OutboxTopics.CANAL_OUTBOX, groupId = "search-index-consumer")
    public void onMessage(String message, Acknowledgment ack) {
        try {
            List<JsonNode> rows = OutboxMessageUtil.extractRows(objectMapper, message);

            if (rows.isEmpty()) {
                ack.acknowledge();
                return;
            }

            for (JsonNode row : rows) {
                JsonNode payloadNode = row.get("payload");
                if (payloadNode == null) {
                    continue;
                }

                JsonNode payload = objectMapper.readTree(payloadNode.asText());
                String entity = text(payload.get("entity"));
                String op = text(payload.get("op"));
                Long id = asLong(payload.get("id"));
                if (!"knowpost".equals(entity) || id == null) {
                    continue;
                }

                // Both soft-delete and upsert overwrite the same document ID to ensure idempotency
                if ("delete".equalsIgnoreCase(op)) {
                    indexService.softDeleteKnowPost(id);
                } else {
                    indexService.upsertKnowPost(id);
                }
            }
            // Commit offset to ensure “processed” semantics
            ack.acknowledge();
        } catch (Exception ignored) {}
    }

    private String text(JsonNode n) {
        return n == null ? null : n.asText();
    }

    private Long asLong(JsonNode n) {
        if (n == null) {
            return null;
        }

        try {
            return Long.parseLong(n.asText());
        } catch (Exception e) {
            return null;
        }
    }
}
