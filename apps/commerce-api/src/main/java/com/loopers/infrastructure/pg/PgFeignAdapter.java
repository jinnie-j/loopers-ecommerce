package com.loopers.infrastructure.pg;

import com.loopers.application.payment.PaymentGateway;
import com.loopers.domain.payment.PaymentMethod;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
public class PgFeignAdapter implements PaymentGateway {

    private final PgFeignClient client;
    @Value("${pg.user-id}") private String userId;

    @Override
    @TimeLimiter(name="pg") @Retry(name="pg") @CircuitBreaker(name="pg")
    public CreatePaymentResponse requestPayment(CreatePaymentRequest req) {
        if (req.method()!= PaymentMethod.CARD)
            throw new IllegalArgumentException("Invalid request method");
        var body = new PgFeignClient.CreatePaymentRequest(req.orderId(), req.cardType(), req.cardNo(), req.amount(), req.callbackUrl());
        return client.request(body, userId);
    }

    @Override @TimeLimiter(name="pg") @Retry(name="pg") @CircuitBreaker(name="pg")
    public PgPaymentDto getPaymentByTx(String transactionId) {
        return client.getByTx(transactionId, userId);
    }

    @Override @TimeLimiter(name="pg") @Retry(name="pg") @CircuitBreaker(name="pg")
    public List<PgPaymentDto> findPaymentsByOrderId(String orderId) {
        return client.findByOrder(orderId, userId);
    }
}
