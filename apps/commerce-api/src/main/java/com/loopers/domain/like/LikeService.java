package com.loopers.domain.like;

import com.loopers.domain.event.LikeCountUpdated;
import com.loopers.infrastructure.product.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class LikeService {

    private final LikeRepository likeRepository;
    private final ProductJpaRepository productJpaRepository;
    private final ApplicationEventPublisher publisher;


    @Transactional
    public LikeInfo like(LikeCommand.Create cmd) {
        Long userId = cmd.userId();
        Long productId = cmd.productId();

        if (likeRepository.existsByUserIdAndProductId(userId, productId)) {
            return LikeInfo.liked(userId, productId);
        }

        boolean changed = false;
        try {
            likeRepository.save(LikeEntity.of(userId, productId));
            changed = true;
        } catch (DataIntegrityViolationException e) {
            changed = false;
        }

        if (changed) {
            long likeCount = likeRepository.countByProductId(productId);

            log.info("[LIKE] publish LikeCountUpdated productId={} likeCount={}", productId, likeCount); // ★
            productJpaRepository.findById(productId).ifPresent(p -> {
                p.setLikeCount(likeCount);
                productJpaRepository.save(p);
            });
            publisher.publishEvent(LikeCountUpdated.of(productId, likeCount));
        }
        return LikeInfo.liked(userId, productId);
    }

    @Transactional
    public LikeInfo unlike(LikeCommand.Create likeCommand) {
        Long userId = likeCommand.userId();
        Long productId = likeCommand.productId();

        int removed = likeRepository.deleteByUserIdAndProductId(userId, productId);
        if (removed == 1) {
            long likeCount = likeRepository.countByProductId(productId);

            // (선택) ProductEntity.likeCount 동기화
            productJpaRepository.findById(productId).ifPresent(p -> {
                p.setLikeCount(likeCount);
                productJpaRepository.save(p);
            });

            publisher.publishEvent(LikeCountUpdated.of(productId, likeCount));
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
