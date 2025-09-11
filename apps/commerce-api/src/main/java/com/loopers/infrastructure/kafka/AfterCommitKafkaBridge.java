package com.loopers.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.like.event.LikeChangedEvent;
import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.domain.order.event.OrderPlacedEvent;
import com.loopers.domain.product.event.ProductLiked;
import com.loopers.domain.product.event.ProductViewed;
import com.loopers.domain.product.event.StockAdjusted;
import com.loopers.infrastructure.order.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Component
@RequiredArgsConstructor
public class AfterCommitKafkaBridge {

    private static final String TOPIC = "catalog-events";
    private static final String ORDER_TOPIC   = "order-events";

    private final KafkaTemplate<Object, Object> kafka;
    private final ObjectMapper om = new ObjectMapper();
    private final OrderJpaRepository orderJpaRepository;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void on(LikeChangedEvent e) {
        try {
            String key = e.productId().toString();
            String json = om.writeValueAsString(new ProductLiked(e.productId(), null, e.delta()));
            ProducerRecord<Object, Object> rec = new ProducerRecord<>(TOPIC, key, json);
            rec.headers().add("event-type", "LIKE".getBytes(StandardCharsets.UTF_8));
            kafka.send(rec);
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
            var order = orderJpaRepository.findByIdWithItems(e.orderId())
                    .orElseThrow(() -> new IllegalStateException("order not found: " + e.orderId()));

            var items = order.getOrderItems().stream()
                    .map(it -> new OrderPlacedEvent.Item(
                            it.getProductId(),
                            it.getPrice(),
                            it.getQuantity()
                    ))
                    .toList();

            String json = om.writeValueAsString(new OrderPlacedEvent(String.valueOf(e.orderId()), items));

            ProducerRecord<Object, Object> rec =
                    new ProducerRecord<>(ORDER_TOPIC, String.valueOf(e.orderId()), json);
            rec.headers().add("event-type", "ORDER".getBytes(java.nio.charset.StandardCharsets.UTF_8));

            kafka.send(rec);
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
