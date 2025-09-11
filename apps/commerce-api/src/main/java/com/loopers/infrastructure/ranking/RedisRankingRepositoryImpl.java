package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.RankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class RedisRankingRepositoryImpl implements RankingRepository {

    private final StringRedisTemplate redis;

    @Override public long size(String key) {
        Long v = redis.opsForZSet().size(key);
        return v == null ? 0L : v;
    }

    @Override
    public List<MemberScore> reverseRangeWithScores(String key, long start, long end) {
        Set<ZSetOperations.TypedTuple<String>> tuples =
                redis.opsForZSet().reverseRangeWithScores(key, start, end);
        if (tuples == null || tuples.isEmpty()) return List.of();

        List<MemberScore> list = new ArrayList<>(tuples.size());
        for (var t : tuples) {
            String member = t.getValue();
            if (member == null || t.getScore() == null) continue;
            try {
                long productId = Long.parseLong(member);
                list.add(new MemberScore(productId, t.getScore()));
            } catch (NumberFormatException ignore) {
            }
        }
        return list;
    }

    @Override
    public Optional<Long> reverseRank(String key, long productId) {
        Long rank = redis.opsForZSet().reverseRank(key, String.valueOf(productId));
        return Optional.ofNullable(rank);
    }

    @Override
    public Optional<Double> score(String key, long productId) {
        Double s = redis.opsForZSet().score(key, String.valueOf(productId));
        return Optional.ofNullable(s);
    }
}
