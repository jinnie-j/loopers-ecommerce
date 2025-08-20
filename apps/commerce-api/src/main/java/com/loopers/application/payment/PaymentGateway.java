package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentMethod;

import java.util.List;

public interface PaymentGateway {

    record CreatePaymentRequest(
            String orderId,
            String amount,
            String callbackUrl,
            PaymentMethod method,
            String cardType,
            String cardNo
    ) {}

    record CreatePaymentResponse(String transactionId, String status) {}
    record PgPaymentDto(String transactionId, String orderId, String status, String amount) {}

    CreatePaymentResponse requestPayment(CreatePaymentRequest req);
    PgPaymentDto getPaymentByTx(String transactionId);
    List<PgPaymentDto> findPaymentsByOrderId(String orderId);
}
