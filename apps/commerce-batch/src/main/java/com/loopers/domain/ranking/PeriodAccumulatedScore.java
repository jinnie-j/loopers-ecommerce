package com.loopers.domain.ranking;

public record PeriodAccumulatedScore(
        long productId,
        double score,
        String weekKey,   // yyyy-MM-dd (월요일)
        String monthKey   // yyyy-MM-dd (1일)
) {}
