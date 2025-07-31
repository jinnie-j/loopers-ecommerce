package com.loopers.domain.like;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Component
public class LikeService {

    private final LikeRepository likeRepository;

    @Transactional
    public LikeInfo like(LikeCommand.Create likeCommand) {
        final LikeEntity likeEntity = LikeEntity.of(likeCommand.userId(), likeCommand.productId());

        return likeRepository.find(likeEntity.getUserId(), likeEntity.getProductId())
                .map(LikeInfo::from)
                .orElseGet(() -> {
                    LikeEntity saved = likeRepository.save(likeEntity);
                    return LikeInfo.from(saved);
                });
    }

    public LikeInfo unlike(LikeCommand.Create likeCommand) {
        long userId = likeCommand.userId();
        long productId = likeCommand.productId();

        boolean exists = likeRepository.find(userId, productId).isPresent();
        if (exists) {
            likeRepository.deleteByUserIdAndProductId(userId, productId);
        }

        return LikeInfo.unliked(userId, productId);
    }

    public Collection<LikeInfo> getLikesByUserId(long userId) {
        List<LikeEntity> likeEntities = likeRepository.findByUserId(userId);
        return likeEntities.stream().map(LikeInfo::from).toList();
    }

    public long countLikes(Long productId) {
        return likeRepository.countByProductId(productId);
    }
}
