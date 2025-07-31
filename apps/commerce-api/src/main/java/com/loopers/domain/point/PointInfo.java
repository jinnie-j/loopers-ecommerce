package com.loopers.domain.point;

public record PointInfo(int balance) {
    public static PointInfo from(PointEntity entity) {
        return new PointInfo(
                entity.getBalance()
        );
    }
}
