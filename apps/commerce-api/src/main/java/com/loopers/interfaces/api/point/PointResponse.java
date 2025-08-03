package com.loopers.interfaces.api.point;

import com.loopers.domain.point.PointInfo;


public record PointResponse(long balance) {

    public static PointResponse from(PointInfo pointInfo) {
        return new PointResponse(pointInfo.balance());
    }
}
