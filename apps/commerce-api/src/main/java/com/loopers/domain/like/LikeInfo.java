package com.loopers.domain.like;

public record LikeInfo(
        Long userId,
        Long productId,
        boolean isLike
) {
    public static LikeInfo from(LikeEntity entity) {
        return new LikeInfo(
                entity.getUserId(),
                entity.getProductId(),
                true
        );
    }
    public static LikeInfo liked(Long userId, Long productId) {
        return new LikeInfo(userId, productId, true);
    }
    public static LikeInfo unliked(Long userId, Long productId) {
        return new LikeInfo(userId, productId, false);
    }
}
