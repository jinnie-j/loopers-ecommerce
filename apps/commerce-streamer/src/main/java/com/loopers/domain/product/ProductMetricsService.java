package com.loopers.domain.product;

import com.loopers.infrastructure.event.EventHandledJpaEntity;
import com.loopers.infrastructure.event.EventHandledKey;
import com.loopers.infrastructure.event.EventHandledJpaRepository;
import com.loopers.infrastructure.product.ProductMetricsDailyJpaRepository;
import com.loopers.infrastructure.product.ProductMetricsEntity;
import com.loopers.infrastructure.product.ProductMetricsJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductMetricsService {
    private final ProductMetricsJpaRepository snapshotJpaRepository;
    private final ProductMetricsDailyJpaRepository dailyRepository;
    private final EventHandledJpaRepository eventHandledJpaRepository;

    private static final String HANDLER = "METRICS";
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    @Transactional
    public void handleLikeChanged(String eventId, long productId, long likeCount, Instant updatedAt) {

        var key = new EventHandledKey(eventId, HANDLER);
        if (eventHandledJpaRepository.existsById(key)) return;

        var snap = snapshotJpaRepository.findById(productId).orElse(null);
        if (snap != null && updatedAt.isBefore(snap.getLastUpdatedAt())) {
            eventHandledJpaRepository.save(EventHandledJpaEntity.of(eventId, HANDLER));
            return;
        }

        long prev = (snap == null) ? 0 : snap.getLikeCount();
        long delta = likeCount - prev;

        if (delta != 0) {
            LocalDate d = LocalDateTime.ofInstant(updatedAt, ZONE).toLocalDate();
            dailyRepository.upsertDaily(d, productId, delta, 0, updatedAt.truncatedTo(ChronoUnit.MILLIS));
        }

        if (snap == null) snapshotJpaRepository.save(ProductMetricsEntity.of(productId, likeCount, updatedAt));
        else { snap.apply(likeCount, updatedAt); snapshotJpaRepository.save(snap); }

        eventHandledJpaRepository.save(EventHandledJpaEntity.of(eventId, HANDLER));
    }

    @Transactional
    public void handleStockAdjusted(String eventId, long productId, Long deltaNullable, java.time.Instant updatedAt) {

        var key = new EventHandledKey(eventId, HANDLER);
        if (eventHandledJpaRepository.existsById(key)) return;

        long delta = Objects.requireNonNullElse(deltaNullable, 0L);
        long sales = (delta < 0) ? -delta : 0;

        if (sales > 0) {
            LocalDate d = LocalDateTime.ofInstant(updatedAt, ZONE).toLocalDate();
            dailyRepository.upsertDaily(d, productId, 0, sales, updatedAt);
        }

        eventHandledJpaRepository.save(EventHandledJpaEntity.of(eventId, HANDLER));
    }
}
