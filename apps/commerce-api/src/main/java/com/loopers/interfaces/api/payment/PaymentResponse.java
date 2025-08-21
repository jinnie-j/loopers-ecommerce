package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentResult;
import com.loopers.domain.payment.PaymentStatus;

public class PaymentResponse {
    public record Summary(
            Long orderId,
            PaymentStatus status
    ) {
        public static Summary from(PaymentResult.Summary s) {
            return new Summary(s.orderId(), s.status());
        }
    }
}
