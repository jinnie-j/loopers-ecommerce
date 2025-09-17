package com.loopers.domain.product.event;

import java.time.Instant;

public record ProductViewed(
        long productId,
        Long viewerId,
        long occurredAt
) {
    public static ProductViewed now(long productId, Long viewerId) {
        return new ProductViewed(productId, viewerId, Instant.now().toEpochMilli());
    }
    public static ProductViewed now(long productId) {
        return new ProductViewed(productId, null, Instant.now().toEpochMilli());
    }
}
