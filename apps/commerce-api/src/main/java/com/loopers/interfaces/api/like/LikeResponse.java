package com.loopers.interfaces.api.like;

import com.loopers.domain.like.LikeInfo;

public record LikeResponse(
        Long userId,
        Long productId,
        boolean liked
) {
    public static LikeResponse from(LikeInfo info) {
        return new LikeResponse(
                info.userId(),
                info.productId(),
                info.isLike()
        );
    }
}
