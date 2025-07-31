package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointEntity;
import com.loopers.domain.point.PointRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PointRepositoryImpl implements PointRepository {
    private final PointJpaRepository pointJpaRepository;

    @Override
    public Optional<PointEntity> findByUserId(long userId){
        return pointJpaRepository.findByUserId(userId);
    }

    @Override
    public PointEntity findById(Long id) {
        return pointJpaRepository.findById(id)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "[id = " + id + "] 포인트 정보를 찾을 수 없습니다."));
    }

    @Override
    public void save(PointEntity point) {
        pointJpaRepository.save(point);
    }
}

