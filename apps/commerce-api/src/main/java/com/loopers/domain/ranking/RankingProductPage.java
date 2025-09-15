package com.loopers.domain.ranking;

import java.util.List;

public record RankingProductPage(
        List<RankingProductItem> items,
        int page,
        int size,
        long total
) {
    public static RankingProductPage empty(int page, int size) {
        return new RankingProductPage(List.of(), page, size, 0L);
    }
}
