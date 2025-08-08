package com.loopers.domain.coupon;

import java.util.Optional;

public interface CouponRepository {
    Optional<CouponEntity> findById(Long id);
    CouponEntity save(CouponEntity couponEntity);
    Optional<CouponEntity> findWithLockById(long id);
}
