package com.loopers.domain.coupon;

import java.time.LocalDateTime;

public record CouponInfo (

    Long couponId,
    String name,
    DiscountType discountType,
    CouponStatus status,
    LocalDateTime expiredAt
    ){
    public static CouponInfo from(CouponEntity entity) {
        return new CouponInfo(
                entity.getId(),
                entity.getName(),
                entity.getDisCountType(),
                entity.getCouponStatus(),
                entity.getExpiredAt()
        );
    }

}
