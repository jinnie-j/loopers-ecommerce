package com.loopers.domain.coupon;

import java.time.LocalDateTime;

public class CouponCommand {

    public record Create(String name, DiscountType discountType, LocalDateTime expiredAt) {}

}
