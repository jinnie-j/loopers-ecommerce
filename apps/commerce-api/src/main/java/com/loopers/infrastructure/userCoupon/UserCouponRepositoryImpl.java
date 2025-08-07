package com.loopers.infrastructure.userCoupon;

import com.loopers.domain.userCoupon.UserCouponEntity;
import com.loopers.domain.userCoupon.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserCouponRepositoryImpl implements UserCouponRepository {
    private final UserCouponJpaRepository userCouponJpaRepository;

    @Override
    public Optional<UserCouponEntity> findByUserIdAndCouponId(Long userId, Long couponId) {
        return userCouponJpaRepository.findByUserIdAndCouponId(userId, couponId);
    }

    @Override
    public UserCouponEntity save(UserCouponEntity userCouponEntity) {
        return userCouponJpaRepository.save(userCouponEntity);
    }

    @Override
    public UserCouponEntity findById(long id) {
        return userCouponJpaRepository.findById(id).orElse(null);
    }
}
