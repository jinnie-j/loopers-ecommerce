package com.loopers.domain.userCoupon;

public record UserCouponInfo (

        Long userId,
        Long couponId,
        UserCouponStatus status
){
    public static UserCouponInfo from(UserCouponEntity entity){
        return new UserCouponInfo(
                entity.getUserId(),
                entity.getCouponId(),
                entity.getStatus()
        );
    }

}
