package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeEntity;
import com.loopers.domain.like.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class LikeRepositoryImpl implements LikeRepository {
    private final LikeJpaRepository likeJpaRepository;

    @Override
    public boolean exists(Long userId, Long productId) {
        return likeJpaRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    public LikeEntity save(LikeEntity likeEntity) {
        return likeJpaRepository.save(likeEntity);
    }

    @Override
    public Optional<LikeEntity> find(long userId, long productId) {
        return likeJpaRepository.findByUserIdAndProductId(userId, productId);
    }

    @Override
    public void deleteByUserIdAndProductId(long userId, long productId) {
        likeJpaRepository.deleteByIdAndProductId(userId, productId);
    }

    @Override
    public List<LikeEntity> findByUserId(long userId) {
        return likeJpaRepository.findByUserId(userId);
    }

    @Override
    public long countByProductId(Long productId) {
        return likeJpaRepository.countByProductId(productId);
    }
}
