package com.loopers.domain.point;

public record PointInfo(long balance) {
    public static PointInfo from(PointEntity entity) {
        return new PointInfo(
                entity.getBalance()
        );
    }
}
