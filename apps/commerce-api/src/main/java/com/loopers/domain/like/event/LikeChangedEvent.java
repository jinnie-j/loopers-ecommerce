package com.loopers.domain.like.event;


public record LikeChangedEvent(Long userId, Long productId, int delta) {
    public static LikeChangedEvent liked(Long userId, Long productId) {return new LikeChangedEvent(userId, productId, +1);}
    public static LikeChangedEvent unliked(Long userId, Long productId) {return new LikeChangedEvent(userId, productId, -1);}
}
