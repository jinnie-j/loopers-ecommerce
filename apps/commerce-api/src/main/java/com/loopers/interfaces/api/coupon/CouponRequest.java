package com.loopers.interfaces.api.coupon;

public class CouponRequest {
    public record Apply(Long userId, Long couponId, long price) {}
}
