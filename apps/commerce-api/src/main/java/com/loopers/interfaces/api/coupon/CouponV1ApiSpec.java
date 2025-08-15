package com.loopers.interfaces.api.coupon;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Coupon API")
@RequestMapping("/api/v1/coupons")
public interface CouponV1ApiSpec {

    @PostMapping
    ApiResponse<CouponResponse> create(@RequestBody @Valid CouponRequest.Create request);

    @GetMapping("/{couponId}")
    ApiResponse<CouponResponse> getCoupon(
            @Schema(name = "쿠폰ID", description = "조회할 쿠폰의 ID") @PathVariable Long couponId
    );

    @PostMapping("/apply")
    ApiResponse<Long> applyCoupon(@RequestBody CouponRequest.Apply request);
}
