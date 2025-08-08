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
    public Optional<PointEntity> getPoint(long userId){
        return pointRepository.findByUserId(userId);
    }

    @Transactional
    public PointEntity chargePoint(long userId, int amount) {
        PointEntity pointEntity = pointRepository.findWithLockByUserId(userId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "[id = " + userId + "] 의 포인트를 찾을 수 없습니다."));

        pointEntity.charge(amount);
        return pointEntity;
    }

    public void usePoints(long userId, Long amount) {
        PointEntity point = pointRepository.findWithLockByUserId(userId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "포인트 정보를 찾을 수 없습니다."));

        if (point.getBalance() < amount) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트가 부족합니다.");
        }

        point.use(amount);
        pointRepository.save(point);
    }
}
