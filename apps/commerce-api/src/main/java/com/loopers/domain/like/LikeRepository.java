package com.loopers.domain.like;


import java.util.List;
import java.util.Optional;

public interface LikeRepository {
    boolean exists(Long userId, Long productId);

    LikeEntity save(LikeEntity likeEntity);

    Optional<LikeEntity> find(long userId, long productId);

    void deleteByUserIdAndProductId(long userId, long productId);

    List<LikeEntity> findByUserId(long userId);

    long countByProductId(Long productId);
}
