package com.loopers.domain.point;

import java.util.Optional;

public interface PointRepository {
    Optional<PointEntity> findByUserId(String userId);
}
