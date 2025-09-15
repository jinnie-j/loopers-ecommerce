package com.loopers.domain.ranking;

public record RankingItem(
        long productId,
        Double score,
        Long rank
) {}
