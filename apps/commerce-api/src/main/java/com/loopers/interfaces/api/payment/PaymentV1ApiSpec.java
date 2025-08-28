package com.loopers.interfaces.api.payment;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name ="Payment V1 API")
@RequestMapping("/api/v1/payments")
public interface PaymentV1ApiSpec {

    /** 결제 요청 */
    @PostMapping
    ApiResponse<PaymentResponse.Summary> request(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestBody @Valid PaymentRequest.Create request
    );

    /** PG 콜백 수신 */
    @PostMapping("/callback")
    ApiResponse<Void> callback(@RequestBody @Valid PaymentRequest.Callback request);

    /** 주문별 결제 조회 */
    @GetMapping("/order/{orderId}")
    ApiResponse<PaymentResponse.Summary> getByOrder(@PathVariable Long orderId);
}
