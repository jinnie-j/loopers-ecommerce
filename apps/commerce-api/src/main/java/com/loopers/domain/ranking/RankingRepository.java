package com.loopers.domain.ranking;

import java.util.List;
import java.util.Optional;

public interface RankingRepository {
    long size(String key);

    List<MemberScore> reverseRangeWithScores(String key, long start, long end);

    Optional<Long> reverseRank(String key, long productId);

    Optional<Double> score(String key, long productId);

    record MemberScore(long productId, double score) {}
}
