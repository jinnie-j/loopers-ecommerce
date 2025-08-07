package com.loopers.infrastructure.userCoupon;

import com.loopers.domain.userCoupon.UserCouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserCouponJpaRepository extends JpaRepository<UserCouponEntity, Long> {
    Optional<UserCouponEntity> findByUserIdAndCouponId(Long userId, Long couponId);
}
