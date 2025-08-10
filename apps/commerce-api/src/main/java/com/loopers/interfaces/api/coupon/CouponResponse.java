package com.loopers.interfaces.api.coupon;

import com.loopers.domain.coupon.CouponInfo;

public record CouponResponse(
        Long couponId,
        String name,
        String discountType,
        String status,
        String expiredAt
) {
    public static CouponResponse from(CouponInfo couponInfo) {
        return new CouponResponse(
                couponInfo.couponId(),
                couponInfo.name(),
                couponInfo.discountType().name(),
                couponInfo.status().name(),
                couponInfo.expiredAt().toString()
        );
    }
}
