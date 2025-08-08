package com.loopers.infrastructure.userCoupon;

import com.loopers.domain.userCoupon.UserCouponEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserCouponJpaRepository extends JpaRepository<UserCouponEntity, Long> {
    Optional<UserCouponEntity> findByUserIdAndCouponId(Long userId, Long couponId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT uc FROM UserCouponEntity uc WHERE uc.userId = :userId AND uc.couponId = :couponId")
    Optional<UserCouponEntity> findWithLockByUserIdAndCouponId(@Param("userId") long userId, @Param("couponId") long couponId);
}
