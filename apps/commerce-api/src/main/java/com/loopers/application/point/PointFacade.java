package com.loopers.application.point;

import com.loopers.domain.point.PointEntity;
import com.loopers.domain.point.PointInfo;
import com.loopers.domain.point.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PointFacade {
    private final PointService pointService;

    public Optional<PointInfo> getPointInfo(long userId){
        return pointService.getPoint(userId)
                .map(PointInfo::from);
    }

    public PointInfo chargePoint(long userId, int amount) {

        PointEntity pointEntity = pointService.chargePoint(userId, amount);
        return PointInfo.from(pointEntity);
    }
}
