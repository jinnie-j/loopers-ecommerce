package com.loopers.domain.like;

import com.loopers.domain.like.event.LikeChangedEvent;
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
            publisher.publishEvent(LikeChangedEvent.of(productId, +1));
        }
        return LikeInfo.liked(userId, productId);
    }

    @Transactional
    public LikeInfo unlike(LikeCommand.Create likeCommand) {
        Long userId = likeCommand.userId();
        Long productId = likeCommand.productId();

        int removed = likeRepository.deleteByUserIdAndProductId(userId, productId);
        if (removed == 1) {
            publisher.publishEvent(LikeChangedEvent.of(productId, -1));
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
