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

            if (!"LIKE_CHANGED".equals(n.path("eventType").asText())) { ack.acknowledge(); return; }

            String eventId = n.path("eventId").asText(null);
            if (eventId == null) {
                eventId = "kafka:%s:%d:%d".formatted(rec.topic(), rec.partition(), rec.offset());
            }

            String aggIdStr   = n.path("aggregateId").asText(null);
            long productId    = (aggIdStr != null) ? Long.parseLong(aggIdStr) : n.path("aggregateId").asLong();
            long likeCount    = n.path("payload").path("likeCount").asLong();
            Instant updatedAt = Instant.parse(n.path("updatedAt").asText());

            metrics.handleLikeChanged(eventId, productId, likeCount, updatedAt);
            ack.acknowledge();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
