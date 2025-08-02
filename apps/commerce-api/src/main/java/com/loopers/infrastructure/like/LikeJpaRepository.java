package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeJpaRepository extends JpaRepository<LikeEntity, Long> {
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    Optional<LikeEntity> findByUserIdAndProductId(long userId, long productId);

    void deleteByIdAndProductId(long userId, long productId);

    List<LikeEntity> findByUserId(long userId);

    long countByProductId(Long productId);
}
