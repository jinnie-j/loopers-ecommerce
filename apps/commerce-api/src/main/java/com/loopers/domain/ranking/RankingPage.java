package com.loopers.domain.ranking;

import java.util.List;

public record RankingPage(
        List<RankingItem> items,
        int page,
        int size,
        long total
) {
    public static RankingPage empty(int page, int size) {
        return new RankingPage(List.of(), page, size, 0L);
    }
}
