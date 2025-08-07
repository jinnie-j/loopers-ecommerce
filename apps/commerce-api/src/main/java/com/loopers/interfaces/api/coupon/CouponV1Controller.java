package com.loopers.interfaces.api.coupon;

import com.loopers.application.coupon.CouponFacade;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CouponV1Controller implements CouponV1ApiSpec{

    private final CouponFacade couponFacade;

    @Override
    public ApiResponse<Long> applyCoupon(@RequestBody CouponRequest.Apply request) {
        long discounted = couponFacade.applyCoupon(request.userId(), request.couponId(), request.price());
        return ApiResponse.success(discounted);
    }
}
