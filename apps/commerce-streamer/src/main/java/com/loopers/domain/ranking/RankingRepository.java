package com.loopers.domain.ranking;

import java.time.Instant;
import java.util.Map;

public interface RankingRepository {
    void incrementScores(String key, Map<String, Double> deltas, Instant expireAt);
}
