package com.loopers.domain.userCoupon;

public class UserCouponCommand {

    public record Use(Long userId, Long couponId) {}

    public record Create(Long userId, Long couponId) {}

    public record Apply(
            Long userId,
            Long couponId,
            long originalPrice
    ) {}

}
