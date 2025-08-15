package com.loopers.domain.point;

import java.util.Optional;

public interface PointRepository {
    Optional<PointEntity> findByUserId(long userId);

    Optional<PointEntity> findById(Long id);

    void save(PointEntity point);

    Optional<PointEntity> findWithLockByUserId(long userId);
}
