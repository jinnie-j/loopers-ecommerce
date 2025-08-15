package com.loopers.interfaces.api.coupon;

import com.loopers.application.coupon.CouponFacade;
import com.loopers.domain.coupon.CouponInfo;
import com.loopers.domain.coupon.CouponService;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CouponV1Controller implements CouponV1ApiSpec{

    private final CouponFacade couponFacade;
    private final CouponService couponService;

    public ApiResponse<CouponResponse> create(@RequestBody @Valid CouponRequest.Create request) {
        var info = couponService.create(request.toCommand());
        return ApiResponse.success(CouponResponse.from(info));
    }

    @Override
    public ApiResponse<CouponResponse> getCoupon(@PathVariable Long couponId) {
        CouponInfo couponInfo = CouponInfo.from(couponService.getAvailableCoupon(couponId));
        return ApiResponse.success(CouponResponse.from(couponInfo));
    }

    @Override
    public ApiResponse<Long> applyCoupon(@Valid @RequestBody CouponRequest.Apply request) {
        long discounted = couponFacade.applyCoupon(request.toCommand());
        return ApiResponse.success(discounted);
    }
}
