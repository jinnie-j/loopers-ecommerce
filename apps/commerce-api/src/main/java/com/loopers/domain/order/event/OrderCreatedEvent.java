package com.loopers.domain.order.event;

import com.loopers.domain.payment.PaymentMethod;

import java.time.Instant;

public record OrderCreatedEvent(
        Long orderId,
        Long userId,
        long totalAmount,
        Long couponId,
        PaymentMethod method,
        String cardType,
        String cardNo,
        long occurredAt
) {
    public static OrderCreatedEvent of(Long orderId, Long userId, long totalAmount, Long couponId) {
        return new OrderCreatedEvent(
                orderId, userId, totalAmount, couponId,
                null, null, null,
                Instant.now().toEpochMilli()
        );
    }
}
