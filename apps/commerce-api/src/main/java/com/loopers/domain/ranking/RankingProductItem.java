package com.loopers.domain.ranking;

public record RankingProductItem(
        Long productId,
        Long rank,
        Double score,
        String name,
        Long price,
        Long stock,
        Long brandId,
        String brandName
) {}
