package com.loopers.interfaces.api.coupon;

import com.loopers.domain.coupon.CouponCommand;
import com.loopers.domain.coupon.DiscountType;
import com.loopers.domain.userCoupon.UserCouponCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class CouponRequest {
    public record Apply(Long userId, Long couponId, long originalPrice) {

            public UserCouponCommand.Apply toCommand() {
            return new UserCouponCommand.Apply(userId, couponId, originalPrice);
        }
    }

    public record Create(
            @NotBlank String name,
            @NotNull DiscountType discountType,
            Long discountAmount,
            Double discountRate,
            @NotBlank String expiredAt
    ) {
        public CouponCommand.Create toCommand() {
            return new CouponCommand.Create(
                    name,
                    discountType,
                    discountAmount,
                    discountRate,
                    LocalDateTime.parse(expiredAt)
            );
        }
    }
}
