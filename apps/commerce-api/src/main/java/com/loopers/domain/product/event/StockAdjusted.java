package com.loopers.domain.product.event;

import java.time.Instant;
import java.util.UUID;

public record StockAdjusted(
        String eventId,
        Long productId,
        long newStock,
        long delta,
        Instant updatedAt
) {
    public static StockAdjusted of(Long productId, long newStock, long delta) {
        return new StockAdjusted(UUID.randomUUID().toString(), productId, newStock, delta, Instant.now());
    }
}
