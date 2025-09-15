package com.loopers.interfaces.consumer.ranking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.ranking.RankingBatchAggregator;
import com.loopers.domain.ranking.DefaultRankingScorePolicy;
import com.loopers.interfaces.consumer.ranking.event.OrderPlacedEvent;
import com.loopers.interfaces.consumer.ranking.event.ProductLikedEvent;
import com.loopers.interfaces.consumer.ranking.event.ProductViewedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RankingEventConsumer {

    private final ObjectMapper om;
    private final RankingBatchAggregator aggregator;

    private final DefaultRankingScorePolicy policy =
            new DefaultRankingScorePolicy(0.1, 0.2, 0.6, true);

    @KafkaListener(
            topics = {"catalog-events", "order-events"},
            containerFactory = "batchKafkaListenerContainerFactory",
            groupId = "ranking-agg"
    )
    public void onBatch(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
        if (records.isEmpty()) {
            ack.acknowledge();
            return;
        }

        Map<String, Double> bucket = new HashMap<>();

        try {
            for (var r : records) {
                String type = header(r, "event-type"); // VIEW / LIKE / ORDER
                if (type == null) continue;

                switch (type) {
                    case "VIEW" -> {
                        var e = om.readValue(r.value(), ProductViewedEvent.class);
                        merge(bucket, String.valueOf(e.productId()), policy.viewDelta());
                    }
                    case "LIKE" -> {
                        var e = om.readValue(r.value(), ProductLikedEvent.class);
                        merge(bucket, String.valueOf(e.productId()), policy.likeDelta(e.delta()));
                    }
                    case "ORDER" -> {
                        var e = om.readValue(r.value(), OrderPlacedEvent.class);
                        for (var it : e.items()) {
                            merge(bucket, String.valueOf(it.productId()), policy.orderDelta(it.price(), it.amount()));
                        }
                    }
                    default -> log.debug("skip unknown event-type={}", type);
                }
            }

            LocalDate today = LocalDate.now();
            aggregator.apply(today, bucket);

            ack.acknowledge();
        } catch (Exception ex) {
            log.error("ranking batch failed; will retry", ex);
        }
    }

    private static void merge(Map<String, Double> bucket, String productId, double delta) {
        bucket.merge(productId, delta, Double::sum);
    }

    private static String header(ConsumerRecord<?, ?> r, String key) {
        var h = r.headers().lastHeader(key);
        return h == null ? null : new String(h.value(), StandardCharsets.UTF_8);
    }
}
