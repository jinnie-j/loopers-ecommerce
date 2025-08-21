package com.loopers.infrastructure.payment.pg;

import com.loopers.application.payment.PaymentGateway;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name="pgClient", url="${pg.base-url}", configuration= PgFeignConfig.class)
public interface PgFeignClient {
    record CreatePaymentRequest(String orderId, String cardType, String cardNo, Long amount, String callbackUrl) {}

    @PostMapping("/api/v1/payments")
    PaymentGateway.CreatePaymentResponse request(@RequestBody CreatePaymentRequest body,
                                                 @RequestHeader("X-USER-ID") String userId);

    @GetMapping("/api/v1/payments/{txId}")
    PaymentGateway.PgPaymentDto getByTx(@PathVariable String txId,
                                        @RequestHeader("X-USER-ID") String userId);

    @GetMapping("/api/v1/payments")
    List<PaymentGateway.PgPaymentDto> findByOrder(@RequestParam String orderId,
                                                  @RequestHeader("X-USER-ID") String userId);
}
