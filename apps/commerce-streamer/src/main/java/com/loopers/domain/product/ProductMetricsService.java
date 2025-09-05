package com.loopers.domain.product;

import com.loopers.infrastructure.event.EventHandledJpaEntity;
import com.loopers.infrastructure.event.EventHandledKey;
import com.loopers.infrastructure.event.EventHandledJpaRepository;
import com.loopers.infrastructure.product.ProductMetricsEntity;
import com.loopers.infrastructure.product.ProductMetricsJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ProductMetricsService {
    private final ProductMetricsJpaRepository snapshotJpaRepository;
    private final EventHandledJpaRepository eventHandledJpaRepository;

    private static final String HANDLER = "METRICS";

    @Transactional
    public void handleLikeChanged(String eventId, long productId, long likeCount, Instant updatedAt) {

        var key = new EventHandledKey(eventId, HANDLER);
        if (eventHandledJpaRepository.existsById(key)) return;

        var snap = snapshotJpaRepository.findById(productId).orElse(null);
        if (snap != null && updatedAt.isBefore(snap.getLastUpdatedAt())) {
            eventHandledJpaRepository.save(EventHandledJpaEntity.of(eventId, HANDLER));
            return;
        }

        if (snap == null) snapshotJpaRepository.save(ProductMetricsEntity.of(productId, likeCount, updatedAt));
        else { snap.apply(likeCount, updatedAt); snapshotJpaRepository.save(snap); }

        eventHandledJpaRepository.save(EventHandledJpaEntity.of(eventId, HANDLER));
    }
}
