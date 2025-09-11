package com.loopers.interfaces.consumer.ranking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.ranking.RankingBatchAggregator;
import com.loopers.interfaces.consumer.ranking.event.OrderPlacedEvent;
import com.loopers.interfaces.consumer.ranking.event.ProductLikedEvent;
import com.loopers.interfaces.consumer.ranking.event.ProductViewedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.support.Acknowledgment;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.mockito.Mockito.*;

class RankingEventConsumerTest {

    private final ObjectMapper om = new ObjectMapper();
    private final RankingBatchAggregator aggregator = mock(RankingBatchAggregator.class);

    private final RankingEventConsumer sut = new RankingEventConsumer(om, aggregator);

    @Test
    @DisplayName("배치 VIEW/LIKE/ORDER를 오늘자 키로 합산하여 Aggregator.apply 호출")
    void batch_aggregates_and_applies_for_today() throws Exception {
        List<ConsumerRecord<String, String>> batch = new ArrayList<>();

        // VIEW: product 10, 3회
        var viewJson = om.writeValueAsString(new ProductViewedEvent(10L, System.currentTimeMillis()));
        batch.add(record("catalog-events", "VIEW", "10", viewJson));
        batch.add(record("catalog-events", "VIEW", "10", viewJson));
        batch.add(record("catalog-events", "VIEW", "10", viewJson));

        // LIKE: product 10, +1
        var likeJson = om.writeValueAsString(new ProductLikedEvent(10L, 1L, +1));
        batch.add(record("catalog-events", "LIKE", "10", likeJson));

        // ORDER: order#1, items = [(10, 10000, 2), (20, 5000, 1)]
        var order = new OrderPlacedEvent("1", List.of(
                new OrderPlacedEvent.Item(10L, 10_000L, 2L),
                new OrderPlacedEvent.Item(20L, 5_000L, 1L)
        ));
        var orderJson = om.writeValueAsString(order);
        batch.add(record("order-events", "ORDER", "1", orderJson));

        Acknowledgment ack = mock(Acknowledgment.class);

        sut.onBatch(batch, ack);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Double>> bucketCap = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<LocalDate> dateCap = ArgumentCaptor.forClass(LocalDate.class);

        verify(aggregator, times(1)).apply(dateCap.capture(), bucketCap.capture());
        assertThat(dateCap.getValue()).isEqualTo(LocalDate.now());

        Map<String, Double> bucket = bucketCap.getValue();
        assertThat(bucket).containsKeys("10", "20");

        // 기대값 계산 (정책: view 0.1, like 0.2*delta, order 0.6*log1p(price*amount))
        double expected10 = (3 * 0.1) + (1 * 0.2) + (0.6 * Math.log1p(10_000L * 2L));
        double expected20 = (0.6 * Math.log1p(5_000L * 1L));

        assertThat(bucket.get("10")).isCloseTo(expected10, offset(1e-6));
        assertThat(bucket.get("20")).isCloseTo(expected20, offset(1e-6));

        verifyNoMoreInteractions(aggregator);
    }

    private static ConsumerRecord<String, String> record(String topic, String type, String key, String json) {
        ConsumerRecord<String, String> r = new ConsumerRecord<>(topic, 0, 0L, key, json);
        Headers headers = r.headers();
        headers.add("event-type", type.getBytes(StandardCharsets.UTF_8));
        return r;
    }
}
