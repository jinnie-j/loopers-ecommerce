package com.loopers.interfaces.consumer.audit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.audit.AuditCommand;
import com.loopers.domain.audit.AuditService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditKafkaConsumer {
    private final ObjectMapper om = new ObjectMapper();
    private final AuditService audit;

    @KafkaListener(topics={"catalog-events"}, groupId="audit-consumer", containerFactory="manualAckFactory")
    public void on(@Payload String json, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                   ConsumerRecord<String,String> rec, Acknowledgment ack){
        try {

            if (json == null || json.isBlank()) {
                ack.acknowledge();
                return;
            }

            JsonNode n = om.readTree(json);
            if (n.isTextual()) {
                n = om.readTree(n.asText());
            }

            String eventId = null;
            if (n.hasNonNull("eventId")) eventId = n.get("eventId").asText();
            else if (n.path("metadata").hasNonNull("eventId")) eventId = n.path("metadata").get("eventId").asText();
            if (eventId == null) {
                eventId = "kafka:%s:%d:%d".formatted(rec.topic(), rec.partition(), rec.offset());
            }

            audit.append(new AuditCommand(eventId, topic, rec.key(), n.toString()));

            ack.acknowledge();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
