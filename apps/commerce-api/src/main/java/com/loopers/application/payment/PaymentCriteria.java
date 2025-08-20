package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentMethod;

public class PaymentCriteria {

    public record RequestPayment(
            Long orderId,
            Long userId,
            Long payableAmount,
            PaymentMethod method,
            Card card,
            Long couponId
    ) {
        public record Card(String cardType, String cardNo) {}
    }

    public record ProcessPgCallback(String transactionId, String status, String approvedAt) {}
}
