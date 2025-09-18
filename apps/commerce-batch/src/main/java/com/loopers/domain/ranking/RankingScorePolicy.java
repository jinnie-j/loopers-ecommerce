package com.loopers.domain.ranking;

public interface RankingScorePolicy {
    double score(long views, long likes, long orderAmount, long orderQty);
}
