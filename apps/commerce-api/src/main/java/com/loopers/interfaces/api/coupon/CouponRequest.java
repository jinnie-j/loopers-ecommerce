package com.loopers.interfaces.api.coupon;

import com.loopers.domain.userCoupon.UserCouponCommand;

public class CouponRequest {
    public record Apply(Long userId, Long couponId, long originalPrice) {

            public UserCouponCommand.Apply toCommand() {
            return new UserCouponCommand.Apply(userId, couponId, originalPrice);
        }
    }
}
