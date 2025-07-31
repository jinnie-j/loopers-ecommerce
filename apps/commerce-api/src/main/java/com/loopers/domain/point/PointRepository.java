package com.loopers.domain.point;

import java.util.Optional;

public interface PointRepository {
    Optional<PointEntity> findByUserId(long userId);
    PointEntity findById(Long id);

    void save(PointEntity point);
}
