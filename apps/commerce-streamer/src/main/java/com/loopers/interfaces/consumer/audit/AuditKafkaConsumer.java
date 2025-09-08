package com.loopers.interfaces.consumer.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.audit.AuditCommand;
import com.loopers.domain.audit.AuditService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditKafkaConsumer {
    private final ObjectMapper om = new ObjectMapper();
    private final AuditService audit;

    @KafkaListener(topics={"catalog-events", "order-events"}, groupId="audit-consumer", containerFactory="manualAckFactory")
    public void on(ConsumerRecord<String,String> rec, Acknowledgment ack){
        try {
            String json = rec.value();
            if (json == null || json.isBlank()) { ack.acknowledge(); return; }

            var n = om.readTree(json);
            if (n.isTextual()) n = om.readTree(n.asText());

            String eventId = n.path("eventId").asText(null);
            if (eventId == null) {
                eventId = "kafka:%s:%d:%d".formatted(rec.topic(), rec.partition(), rec.offset());
            }

            audit.appendIfFirst(new AuditCommand(eventId, rec.topic(), rec.key(), n.toString()));
            ack.acknowledge();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
