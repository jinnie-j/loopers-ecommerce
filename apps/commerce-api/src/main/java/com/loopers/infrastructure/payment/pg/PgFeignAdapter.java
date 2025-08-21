package com.loopers.infrastructure.payment.pg;

import com.loopers.application.payment.PaymentGateway;
import com.loopers.domain.payment.PaymentMethod;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class PgFeignAdapter implements PaymentGateway {

    private final PgFeignClient client;
    @Value("${pg.user-id}") private String userId;

    // 결제 생성
    @Override
    @Retry(name="pg", fallbackMethod = "requestPaymentFallback")
    @CircuitBreaker(name="pg")
    public CreatePaymentResponse requestPayment(CreatePaymentRequest req) {
        if (req.method()!= PaymentMethod.CARD)
            throw new IllegalArgumentException("Invalid request method");
        var body = new PgFeignClient.CreatePaymentRequest(req.orderId(), req.cardType(), req.cardNo(), req.amount(), req.callbackUrl());
        return client.request(body, userId);
    }

    // 상태 조회
    @Override
    @Retry(name="pg", fallbackMethod = "getPaymentByTxFallback") @CircuitBreaker(name="pg")
    public PgPaymentDto getPaymentByTx(String transactionId) {
        return client.getByTx(transactionId, userId);
    }

    // 주문별 조회
    @Override
    @Retry(name="pg", fallbackMethod = "findPaymentsByOrderIdFallback") @CircuitBreaker(name="pg")
    public List<PgPaymentDto> findPaymentsByOrderId(String orderId) {
        return client.findByOrder(orderId, userId);
    }

    // 결제 생성 폴백: null 리턴 → Facade에서 REQUESTED 유지, 리컨실
    private CreatePaymentResponse requestPaymentFallback(CreatePaymentRequest req, Throwable t) {
        log.warn("PG create failed(orderId={}): {}", req.orderId(), t.toString());
        return new CreatePaymentResponse(null, "RETRY_LATER");
    }

    // 상태 조회 폴백: null 리턴 → 다음 주기 리컨실
    private PgPaymentDto getPaymentByTxFallback(String transactionId, Throwable t) {
        log.debug("PG getByTx fallback(txId={}): {}", transactionId, t.toString());
        return new PgPaymentDto(transactionId, null, "UNKNOWN", null);
    }

    // 주문별 조회 폴백: 빈 리스트
    private List<PgPaymentDto> findPaymentsByOrderIdFallback(String orderId, Throwable t) {
        log.debug("PG findByOrder fallback(orderId={}): {}", orderId, t.toString());
        return List.of();
    }
}
