package com.loopers.interfaces.consumer.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.cache.CacheEvictService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class CacheKafkaConsumer {
    private final ObjectMapper om;
    private final CacheEvictService cache;

    public CacheKafkaConsumer(ObjectMapper om, CacheEvictService cache) {
        this.om = om;
        this.cache = cache;
    }

    @KafkaListener(topics="catalog-events", groupId="cache-consumer", containerFactory="manualAckFactory")
    public void on(ConsumerRecord<String,String> rec, Acknowledgment ack){
        try {
            String json = rec.value();
            if (json == null || json.isBlank()) { ack.acknowledge(); return; }

            var n = om.readTree(json);
            if (n.isTextual()) n = om.readTree(n.asText());

            String type = n.path("eventType").asText("");
            String aggIdStr = n.path("aggregateId").asText(null);
            long productId  = (aggIdStr != null) ? Long.parseLong(aggIdStr) : n.path("aggregateId").asLong();

            switch (type) {
                case "LIKE_CHANGED"   -> cache.onLikeChanged(productId);
                case "STOCK_ADJUSTED" -> cache.onStockAdjusted(productId);
                default -> { }
            }
            ack.acknowledge();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
