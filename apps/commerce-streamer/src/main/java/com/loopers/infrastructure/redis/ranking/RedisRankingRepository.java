package com.loopers.infrastructure.redis.ranking;

import com.loopers.domain.ranking.RankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class RedisRankingRepository implements RankingRepository {

    private final StringRedisTemplate redis;

    @Override
    public void incrementScores(String key, Map<String, Double> deltas, Instant expireAt) {
        redis.executePipelined((RedisCallback<Object>) connection -> {
            var s = (StringRedisSerializer) redis.getStringSerializer();
            byte[] k = s.serialize(key);
            deltas.forEach((member, delta) ->
                    connection.zIncrBy(k, delta, s.serialize(member)));
            connection.expireAt(k, expireAt.getEpochSecond());
            return null;
        });
    }
}
