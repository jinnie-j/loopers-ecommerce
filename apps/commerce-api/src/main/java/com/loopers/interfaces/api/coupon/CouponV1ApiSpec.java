package com.loopers.interfaces.api.coupon;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Coupon API")
@RequestMapping("/api/v1/coupons")
public interface CouponV1ApiSpec {

    @PostMapping("/apply")
    ApiResponse<Long> applyCoupon(@RequestBody CouponRequest.Apply request);
}
