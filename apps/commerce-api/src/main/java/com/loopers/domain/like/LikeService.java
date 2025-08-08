package com.loopers.domain.like;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Component
public class LikeService {

    private final LikeRepository likeRepository;

    @Transactional
    public LikeInfo like(LikeCommand.Create cmd) {
        LikeEntity entity = LikeEntity.of(cmd.userId(), cmd.productId());
        try {
            LikeEntity saved = likeRepository.save(entity);
            return LikeInfo.from(saved);
        } catch (DataIntegrityViolationException e) {

            return likeRepository.find(cmd.userId(), cmd.productId())
                    .map(LikeInfo::from)
                    .orElseGet(() -> LikeInfo.liked(cmd.userId(), cmd.productId()));
        }
    }
    @Transactional
    public LikeInfo unlike(LikeCommand.Create likeCommand) {
        likeRepository.deleteByUserIdAndProductId(likeCommand.userId(), likeCommand.productId());
        return LikeInfo.unliked(likeCommand.userId(), likeCommand.productId());
    }

    public Collection<LikeInfo> getLikesByUserId(long userId) {
        List<LikeEntity> likeEntities = likeRepository.findByUserId(userId);
        return likeEntities.stream().map(LikeInfo::from).toList();
    }

    public long countByProductId(Long productId) {
        return likeRepository.countByProductId(productId);
    }
}
