package com.loopers.interfaces.api.ranking;

import com.loopers.domain.ranking.RankingProductItem;

public record RankingItemResponse(
        Long productId,
        Long rank,
        Double score,
        String name,
        Long price,
        Long stock,
        Long brandId,
        String brandName
) {
    public static RankingItemResponse from(RankingProductItem productItem) {
        return new RankingItemResponse(
                productItem.productId(),
                productItem.rank(),
                productItem.score(),
                productItem.name(),
                productItem.price(),
                productItem.stock(),
                productItem.brandId(),
                productItem.brandName()
        );
    }
}
