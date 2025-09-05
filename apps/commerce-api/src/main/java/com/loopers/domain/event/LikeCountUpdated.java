package com.loopers.domain.event;

import java.time.Instant;
import java.util.UUID;

public record LikeCountUpdated(
        String eventId,
        Long productId,
        long likeCount,
        Instant updatedAt
) {
    public static LikeCountUpdated of(Long productId, long likeCount) {
        return new LikeCountUpdated(
                UUID.randomUUID().toString(),
                productId,
                likeCount,
                Instant.now()
        );
    }
}
