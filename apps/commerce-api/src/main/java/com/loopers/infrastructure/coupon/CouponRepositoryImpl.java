package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.CouponEntity;
import com.loopers.domain.coupon.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class CouponRepositoryImpl implements CouponRepository {

    private final CouponJpaRepository couponJpaRepository;

    @Override
    public Optional<CouponEntity> findById(Long id) {
        return couponJpaRepository.findById(id);
    }

    @Override
    public CouponEntity save(CouponEntity couponEntity) {
        return couponJpaRepository.save(couponEntity);
    }
}
