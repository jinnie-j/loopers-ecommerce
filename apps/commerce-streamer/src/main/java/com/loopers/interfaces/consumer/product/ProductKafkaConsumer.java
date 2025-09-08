package com.loopers.interfaces.consumer.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.product.ProductMetricsService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ProductKafkaConsumer {
    private final ObjectMapper om = new ObjectMapper();
    private final ProductMetricsService metrics;

    @KafkaListener(topics="catalog-events", groupId="metrics-consumer", containerFactory="manualAckFactory")
    public void on(ConsumerRecord<String,String> rec, Acknowledgment ack){
        try {
            String json = rec.value();
            if (json == null || json.isBlank()) { ack.acknowledge(); return; }

            var n = om.readTree(json);
            if (n.isTextual()) n = om.readTree(n.asText());

            String type = n.path("eventType").asText("");
            String eventId = n.path("eventId").asText(null);
            if (eventId == null) eventId = "kafka:%s:%d:%d".formatted(rec.topic(), rec.partition(), rec.offset());

            long productId = n.path("aggregateId").isTextual()
                    ? Long.parseLong(n.path("aggregateId").asText())
                    : n.path("aggregateId").asLong();

            Instant updatedAt = Instant.parse(n.path("updatedAt").asText());

            switch (type) {
                case "LIKE_CHANGED" -> {
                    long likeCount = n.path("payload").path("likeCount").asLong();
                    metrics.handleLikeChanged(eventId, productId, likeCount, updatedAt);
                }
                case "STOCK_ADJUSTED" -> {
                    Long delta = n.path("payload").path("delta").isMissingNode()
                            ? null : n.path("payload").path("delta").asLong();
                    metrics.handleStockAdjusted(eventId, productId, delta, updatedAt);
                }
                default -> { /* ignore others */ }
            }

            ack.acknowledge();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
