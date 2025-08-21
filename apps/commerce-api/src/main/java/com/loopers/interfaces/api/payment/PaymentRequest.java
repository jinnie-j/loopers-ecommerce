package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentCriteria;
import com.loopers.domain.payment.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;


public class PaymentRequest {
    public record Create(
            @NotNull Long orderId,
            @NotNull Long userId,
            @NotNull Long payableAmount,
            @NotNull PaymentMethod method,
            Card card,
            Long couponId
    ) {
        public PaymentCriteria.RequestPayment toCriteria(Long userId) {
            PaymentCriteria.RequestPayment.Card c =
                    (card == null) ? null : new PaymentCriteria.RequestPayment.Card(card.cardType, card.cardNo);
            return new PaymentCriteria.RequestPayment(orderId, userId, payableAmount, method, c, couponId);
        }
    }

    public record Card(
            @Schema String cardType,
            @Schema String cardNo
    ) {}

    public record Callback(
            @NotNull String transactionId,
            @NotNull String status,
            String approvedAt
    ) {
        public PaymentCriteria.ProcessPgCallback toCriteria() {
            return new PaymentCriteria.ProcessPgCallback(transactionId, status, approvedAt);
        }
    }
}
