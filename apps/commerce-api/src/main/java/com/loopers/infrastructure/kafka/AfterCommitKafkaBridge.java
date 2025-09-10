package com.loopers.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.event.LikeCountUpdated;
import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.domain.product.event.ProductViewed;
import com.loopers.domain.product.event.StockAdjusted;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Component
@RequiredArgsConstructor
public class AfterCommitKafkaBridge {

    private static final String TOPIC = "catalog-events";
    private static final String ORDER_TOPIC   = "order-events";

    private final KafkaTemplate<Object, Object> kafka;
    private final ObjectMapper om = new ObjectMapper();

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void on(LikeCountUpdated e) {
        try {
            String key = e.productId().toString();
            String json = om.writeValueAsString(Map.of(
                    "eventId",     e.eventId(),
                    "eventType",   "LIKE_CHANGED",
                    "aggregateType","PRODUCT",
                    "aggregateId", key,
                    "updatedAt",   e.updatedAt().toString(),
                    "producerApp", "commerce-api",
                    "payload",     Map.of("likeCount", e.likeCount())
            ));

            kafka.send(TOPIC, key, json).get();

        } catch (Exception ex) {
            throw new RuntimeException("Kafka publish failed", ex);
        }
    }

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void on(StockAdjusted e) {
        try {
            String key = e.productId().toString();
            String json = om.writeValueAsString(Map.of(
                    "eventId",      e.eventId(),
                    "eventType",    "STOCK_ADJUSTED",
                    "aggregateType","PRODUCT",
                    "aggregateId",  key,
                    "updatedAt",    e.updatedAt().toString(),
                    "producerApp",  "commerce-api",
                    "payload",      Map.of("newStock", e.newStock()
                    )
            ));
            kafka.send(TOPIC, key, json).get();
        } catch (Exception ex) {
            throw new RuntimeException("Kafka publish failed", ex);
        }
    }
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void on(OrderCreatedEvent e) {
        try {
            String key = e.orderId().toString();

            // payload는 null 가능성이 있어 Map.of 대신 가변 Map 사용
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId",      e.userId());
            payload.put("totalAmount", e.totalAmount());
            payload.put("couponId",    e.couponId());
            payload.put("method",      e.method() != null ? e.method().name() : null);
            payload.put("cardType",    e.cardType());

            String json = om.writeValueAsString(Map.of(
                    "eventId",       UUID.randomUUID().toString(),
                    "eventType",     "ORDER_CREATED",
                    "aggregateType", "ORDER",
                    "aggregateId",   key,
                    "updatedAt",     Instant.now().toString(),
                    "producerApp",   "commerce-api",
                    "payload",       payload
            ));

            kafka.send(ORDER_TOPIC, key, json).get();
        } catch (Exception ex) {
            throw new RuntimeException("Kafka publish failed", ex);
        }
    }

    @EventListener
    public void on(ProductViewed event) {
        try {
            Object key = String.valueOf(event.productId());
            Object payload = om.writeValueAsString(event);

            ProducerRecord<Object, Object> rec = new ProducerRecord<>(TOPIC, key, payload);
            rec.headers().add("event-type", "VIEW".getBytes(StandardCharsets.UTF_8));

            kafka.send(rec);
        } catch (Exception ex) {
            throw new RuntimeException("Kafka publish failed", ex);
        }
    }

}
