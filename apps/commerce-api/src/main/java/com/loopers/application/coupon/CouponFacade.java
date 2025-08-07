package com.loopers.application.coupon;

import com.loopers.domain.coupon.CouponEntity;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.userCoupon.UserCouponCommand;
import com.loopers.domain.userCoupon.UserCouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CouponFacade {

    private final CouponService couponService;
    private final UserCouponService userCouponService;

    public long applyCoupon(UserCouponCommand.Apply command) {
        userCouponService.validateOwnedAndUnused(new UserCouponCommand.Use(command.userId(), command.couponId()));

        CouponEntity coupon = couponService.getAvailableCoupon(command.couponId());
        long discountedPrice = couponService.applyDiscount(coupon, command.originalPrice());

        userCouponService.useCoupon(new UserCouponCommand.Use(command.userId(), command.couponId()));
        return discountedPrice;
    }
}
