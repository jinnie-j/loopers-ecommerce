package com.loopers.domain.like;


import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LikeRepository {
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    LikeEntity save(LikeEntity likeEntity);

    Optional<LikeEntity> find(long userId, long productId);

    @Modifying
    @Query("delete from LikeEntity l where l.userId = :userId and l.productId = :productId")
    int deleteByUserIdAndProductId(long userId, long productId);

    List<LikeEntity> findByUserId(long userId);

    long countByProductId(Long productId);
}
