package com.loopers.domain.order.event;

import com.loopers.domain.payment.PaymentMethod;

public record OrderCreatedEvent(
        Long orderId,
        Long userId,
        long totalAmount, // 최종 결제 대상 금액(할인 반영)
        Long couponId,
        PaymentMethod method,
        String cardType,
        String cardNo
) {}
