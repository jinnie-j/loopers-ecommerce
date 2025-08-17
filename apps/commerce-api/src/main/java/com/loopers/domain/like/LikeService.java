package com.loopers.domain.like;

import com.loopers.config.redis.RedisConfig;
import com.loopers.infrastructure.product.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Component
public class LikeService {

    private final LikeRepository likeRepository;
    private final ProductJpaRepository productJpaRepository;

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = RedisConfig.PRODUCT_DETAIL, key = "#cmd.productId()"),
            @CacheEvict(cacheNames = RedisConfig.PRODUCT_LIST,   allEntries = true)
    })
    public LikeInfo like(LikeCommand.Create cmd) {
        Long userId = cmd.userId();
        Long productId = cmd.productId();

        if (likeRepository.existsByUserIdAndProductId(userId, productId)) {
            return LikeInfo.liked(userId, productId);
        }
        try {
            likeRepository.save(LikeEntity.of(userId, productId));   // UNIQUE(user_id, product_id)로 중복 방지
            productJpaRepository.incrementLikeCount(productId);
        } catch (DataIntegrityViolationException e) {

        }
        return LikeInfo.liked(userId, productId);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = RedisConfig.PRODUCT_DETAIL, key = "#cmd.productId()"),
            @CacheEvict(cacheNames = RedisConfig.PRODUCT_LIST,   allEntries = true)
    })
    public LikeInfo unlike(LikeCommand.Create likeCommand) {
        Long userId = likeCommand.userId();
        Long productId = likeCommand.productId();

        int removed = likeRepository.deleteByUserIdAndProductId(userId, productId);
        if (removed == 1) {
            productJpaRepository.decrementLikeCount(productId);
        }
        return LikeInfo.unliked(userId, productId);
    }

    public Collection<LikeInfo> getLikesByUserId(long userId) {
        List<LikeEntity> likeEntities = likeRepository.findByUserId(userId);
        return likeEntities.stream().map(LikeInfo::from).toList();
    }

    public long countByProductId(Long productId) {
        return likeRepository.countByProductId(productId);
    }
}
