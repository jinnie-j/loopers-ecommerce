package com.loopers.interfaces.api.ranking;

import java.util.List;

public record RankingPageResponse(
        int page,
        int size,
        long total,
        List<RankingItemResponse> items
) {}
