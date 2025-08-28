package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentStatus;

public class PaymentResult {
    public record Summary(
            Long id,
            Long orderId,
            Long userId,
            Long amount,
            PaymentStatus status,
            String pgTxId,
            Long couponId
    ) {
        public static Summary from(PaymentEntity e) {
            return new Summary(
                    e.getId(), e.getOrderId(), e.getUserId(), e.getAmount(),
                    e.getStatus(), e.getPgTxId(), e.getCouponId()
            );
        }
    }
}
