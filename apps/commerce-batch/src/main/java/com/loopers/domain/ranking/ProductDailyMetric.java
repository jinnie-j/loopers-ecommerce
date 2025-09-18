package com.loopers.domain.ranking;

import java.time.LocalDate;

public record ProductDailyMetric(
        LocalDate metricDate,
        long productId,
        long views,
        long likes,
        long orderQty
) {}
