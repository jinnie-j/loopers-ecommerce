package com.loopers.application.point;

import com.loopers.domain.point.PointEntity;

public record PointInfo(int balance) {
    public static PointInfo from(PointEntity entity) {
        return new PointInfo(
                entity.getBalance()
        );
    }
}
