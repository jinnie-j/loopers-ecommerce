package com.loopers.domain.product;

import com.loopers.infrastructure.event.EventHandledJpaEntity;
import com.loopers.infrastructure.event.EventHandledJpaRepository;
import com.loopers.infrastructure.event.EventHandledKey;
import com.loopers.infrastructure.product.ProductMetricsDailyJpaRepository;
import com.loopers.infrastructure.product.ProductMetricsEntity;
import com.loopers.infrastructure.product.ProductMetricsJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductMetricsServiceTest {
    @Mock
    ProductMetricsJpaRepository productMetricsJpaRepository;
    @Mock
    ProductMetricsDailyJpaRepository productMetricsDailyJpaRepository;
    @Mock
    EventHandledJpaRepository eventHandledJpaRepository;

    ProductMetricsService service;

    @BeforeEach
    void setUp() {
        service = new ProductMetricsService(productMetricsJpaRepository, productMetricsDailyJpaRepository, eventHandledJpaRepository);
    }

    @Test
    void shouldUpsertLikeDeltaAndSnapshot_onFirstLikeEvent() {
        String eventId = "like-1";
        long productId = 1014L;
        long likeCount = 7L;
        Instant updatedAt = Instant.parse("2025-09-05T06:00:00Z");
        LocalDate dayKst = LocalDateTime.ofInstant(updatedAt, ZoneId.of("Asia/Seoul")).toLocalDate();

        when(eventHandledJpaRepository.existsById(new EventHandledKey(eventId, "METRICS"))).thenReturn(false);
        when(productMetricsJpaRepository.findById(productId)).thenReturn(java.util.Optional.empty());

        service.handleLikeChanged(eventId, productId, likeCount, updatedAt);

        // upsertDaily(like +7, sales 0)
        verify(productMetricsDailyJpaRepository).upsertDaily(eq(dayKst), eq(productId), eq(7L), eq(0L), eq(updatedAt));

        verify(productMetricsJpaRepository).save(argThat(s ->
                s.getProductId().equals(productId)
                        && s.getLikeCount() == 7L
                        && s.getLastUpdatedAt().equals(updatedAt)
        ));

        verify(eventHandledJpaRepository).save(argThat(e ->
                e.getEventId() != null
                        && "METRICS".equals(e.getHandlerName())
                        && eventId.equals(e.getEventId())
        ));
    }

    @Test
    void shouldSkipUpsert_butMarkHandled_onOutOfOrderLikeEvent() {
        String eventId = "like-old";
        long productId = 1014L;
        long likeCount = 5L;
        Instant oldTs = Instant.parse("2025-09-05T06:00:00Z");
        Instant newTs = Instant.parse("2025-09-05T08:00:00Z");

        var snap = ProductMetricsEntity.of(productId, 10L, newTs);
        when(eventHandledJpaRepository.existsById(new EventHandledKey(eventId, "METRICS"))).thenReturn(false);
        when(productMetricsJpaRepository.findById(productId)).thenReturn(java.util.Optional.of(snap));

        service.handleLikeChanged(eventId, productId, likeCount, oldTs);

        verifyNoInteractions(productMetricsDailyJpaRepository);
        verify(eventHandledJpaRepository).save(argThat(e ->
                e.getEventId() != null
                        && "METRICS".equals(e.getHandlerName())
                        && eventId.equals(e.getEventId())
        ));
    }

    @Test
    void shouldAccumulateSales_onNegativeStockDelta() {
        String eventId = "stock-1";
        long productId = 2002L;
        Long delta = -3L; // 재고 3 감소 → 판매량 +3
        Instant ts = Instant.parse("2025-09-06T02:00:00Z");
        LocalDate dayKst = LocalDateTime.ofInstant(ts, ZoneId.of("Asia/Seoul")).toLocalDate();

        when(eventHandledJpaRepository.existsById(new EventHandledKey(eventId, "METRICS"))).thenReturn(false);

        service.handleStockAdjusted(eventId, productId, delta, ts);

        verify(productMetricsDailyJpaRepository).upsertDaily(eq(dayKst), eq(productId), eq(0L), eq(3L), eq(ts));
        verify(eventHandledJpaRepository).save(argThat(e ->
                e.getEventId() != null
                        && "METRICS".equals(e.getHandlerName())
                        && eventId.equals(e.getEventId())
        ));
    }

    @Test
    void shouldNotAccumulateSales_onZeroOrPositiveStockDelta() {
        String eventId = "stock-2";
        long productId = 2002L;
        Instant ts = Instant.parse("2025-09-06T03:00:00Z");

        when(eventHandledJpaRepository.existsById(new EventHandledKey(eventId, "METRICS"))).thenReturn(false);

        // delta = 0
        service.handleStockAdjusted(eventId, productId, 0L, ts);
        // delta = +5
        service.handleStockAdjusted(eventId, productId, 5L, ts);

        verifyNoInteractions(productMetricsDailyJpaRepository); // 판매량 누적 안 함
        verify(eventHandledJpaRepository, times(2)).save(argThat(e ->
                e.getEventId() != null &&
                        "METRICS".equals(e.getHandlerName()) &&
                        eventId.equals(e.getEventId())
        ));
    }
}
