package com.loopers.interfaces.consumer.cache;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.domain.cache.CacheEvictService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class CacheKafkaConsumerTest {

    @Mock
    CacheEvictService cache;
    @Mock
    Acknowledgment ack;

    @Test
    public void shouldEvictCacheAndAck_onLikeChanged() throws Exception {

        var consumer = new CacheKafkaConsumer(new ObjectMapper(), cache);
        long productId = 1014L;
        String json = """
        {
          "eventType":"LIKE_CHANGED",
          "aggregateType":"PRODUCT",
          "aggregateId":"%d",
          "updatedAt":"2025-09-05T06:13:26Z",
          "payload":{"likeCount":1}
        }
        """.formatted(productId);

        var rec = new ConsumerRecord<>("catalog-events", 1, 42L, String.valueOf(productId), json);

        consumer.on(rec, ack);

        verify(cache).onLikeChanged(productId);
        verify(ack).acknowledge();
        verifyNoMoreInteractions(cache, ack);
    }

    @Test
    public void shouldEvictCacheAndAck_onStockAdjusted() throws Exception {
        var consumer = new CacheKafkaConsumer(new ObjectMapper(), cache);
        long productId = 2002L;
        String json = """
        {
          "eventType":"STOCK_ADJUSTED",
          "aggregateType":"PRODUCT",
          "aggregateId":"%d",
          "updatedAt":"2025-09-05T06:14:00Z",
          "payload":{"newStock":7}
        }
        """.formatted(productId);

        var rec = new ConsumerRecord<>("catalog-events", 0, 7L, String.valueOf(productId), json);

        consumer.on(rec, ack);

        verify(cache).onStockAdjusted(productId);
        verify(ack).acknowledge();
        verifyNoMoreInteractions(cache, ack);
    }

    @Test
    public void shouldAckOnly_onIrrelevantEvent() throws Exception {
        var consumer = new CacheKafkaConsumer(new ObjectMapper(), cache);
        String json = """
        {
          "eventType":"IGNORED",
          "aggregateType":"PRODUCT",
          "aggregateId":"9999",
          "updatedAt":"2025-09-05T06:14:00Z",
          "payload":{}
        }
        """;

        var rec = new ConsumerRecord<>("catalog-events", 2, 1L, "9999", json);

        consumer.on(rec, ack);

        verifyNoInteractions(cache);
        verify(ack).acknowledge();
    }
}
