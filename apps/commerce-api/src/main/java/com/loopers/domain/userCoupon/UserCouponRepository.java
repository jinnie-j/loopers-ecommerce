package com.loopers.domain.userCoupon;


import java.util.Optional;

public interface UserCouponRepository {
    Optional<UserCouponEntity> findByUserIdAndCouponId(Long userId, Long couponId);
    UserCouponEntity save(UserCouponEntity userCoupon);
    UserCouponEntity findById(long id);
}
