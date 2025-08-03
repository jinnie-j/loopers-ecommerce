package com.loopers.interfaces.api.point;


public class PointRequest {

    public record PointChargeRequest(int amount) {
        public PointChargeRequest {
            if (amount <= 0) {
                throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
            }
        }
    }
}
