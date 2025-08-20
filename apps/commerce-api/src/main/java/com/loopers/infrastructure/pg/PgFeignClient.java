package com.loopers.infrastructure.pg;

import com.loopers.application.payment.PaymentGateway;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name="pgClient", url="${pg.base-url}", configuration=PgFeignConfig.class)
public interface PgFeignClient {
    record CreatePaymentRequest(String orderId, String cardType, String cardNo, String amount, String callbackUrl) {}

    @PostMapping("/api/v1/payments")
    PaymentGateway.CreatePaymentResponse request(@RequestBody CreatePaymentRequest body,
                                                 @RequestHeader("X-USER-ID") String userId);

    @GetMapping("/api/v1/payments/{txId}")
    PaymentGateway.PgPaymentDto getByTx(@PathVariable String txId,
                                        @RequestHeader("X-USER-ID") String userId);

    @GetMapping("/api/v1/payments")
    PaymentGateway.PgPaymentDto findByOrder(@RequestParam String orderId,
                                                                 @RequestHeader("X-USER-ID") String userId);
}
