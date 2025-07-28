package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PointService {

    private final PointRepository pointRepository;

    @Transactional(readOnly = true)
    public Optional<PointEntity> getPoint(String userId){
        return pointRepository.findByUserId(userId);
    }

    public PointEntity chargePoint(String userId, int amount) {
        PointEntity pointEntity = pointRepository.findByUserId(userId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "[id = " + userId + "] 의 포인트를 찾을 수 없습니다."));

        pointEntity.charge(amount);
        return pointEntity;
    }
}
