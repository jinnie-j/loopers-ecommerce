package com.loopers.interfaces.consumer.product;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.product.ProductMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ProductKafkaConsumer {
    private final ObjectMapper om = new ObjectMapper();
    private final ProductMetricsService metrics;

    @KafkaListener(topics="catalog-events", groupId="metrics-consumer", containerFactory="manualAckFactory")
    public void on(@Payload String json, Acknowledgment ack){
        try {
            if (json == null || json.isBlank()) { ack.acknowledge(); return; }

            JsonNode n = om.readTree(json);
            if (n.isTextual()) {
                n = om.readTree(n.asText());
            }

            if (!"LIKE_CHANGED".equals(n.path("eventType").asText())) {
                ack.acknowledge(); return;
            }

            String eventId   = n.path("eventId").asText(null);
            String aggIdStr  = n.path("aggregateId").asText(null);
            long productId   = aggIdStr != null ? Long.parseLong(aggIdStr) : n.path("aggregateId").asLong(); // "1014"도 안전
            long likeCount   = n.path("payload").path("likeCount").asLong();
            Instant updatedAt= Instant.parse(n.path("updatedAt").asText());

            // 멱등/최신성 체크는 서비스에서 수행
            metrics.handleLikeChanged(eventId != null ? eventId : "kafka:catalog-events::",
                    productId, likeCount, updatedAt);

            ack.acknowledge();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
