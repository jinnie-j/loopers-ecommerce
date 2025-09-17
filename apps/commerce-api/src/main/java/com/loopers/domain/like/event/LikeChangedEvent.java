package com.loopers.domain.like.event;

import java.time.Instant;

public record LikeChangedEvent(
        Long userId,
        Long productId,
        int  delta,
        long occurredAt
) {
    public static LikeChangedEvent liked(Long userId, Long productId) {
        return new LikeChangedEvent(userId, productId, +1, Instant.now().toEpochMilli());
    }
    public static LikeChangedEvent unliked(Long userId, Long productId) {
        return new LikeChangedEvent(userId, productId, -1, Instant.now().toEpochMilli());
    }
    public static LikeChangedEvent of(Long productId, int delta) {
        int d = delta > 0 ? +1 : -1;
        return new LikeChangedEvent(null, productId, d, Instant.now().toEpochMilli());
    }
}
