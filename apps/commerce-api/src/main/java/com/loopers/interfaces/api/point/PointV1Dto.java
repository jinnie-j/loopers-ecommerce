package com.loopers.interfaces.api.point;

import com.loopers.application.point.PointInfo;

import java.util.Optional;

public class PointV1Dto {
    public record PointResponse(int balance) {
        public static PointResponse from(Optional<PointInfo> pointInfo) {
            return new PointResponse(
                    pointInfo.get().balance()
            );
        }
    }

    public record PointChargeRequest(int amount) {
        public PointChargeRequest {
            if (amount <= 0) {
                throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
            }
        }

    }
}
