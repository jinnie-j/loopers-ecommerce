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
public class
PointRepositoryImpl implements PointRepository {
    private final PointJpaRepository pointJpaRepository;

    @Override
    public Optional<PointEntity> findByUserId(long userId){
        return pointJpaRepository.findByUserId(userId);
    }

    @Override
    public Optional<PointEntity> findById(Long id) {
        return pointJpaRepository.findById(id);
    }

    @Override
    public void save(PointEntity point) {
        pointJpaRepository.save(point);
    }
}

