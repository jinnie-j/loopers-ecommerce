package com.loopers.domain.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentService {

    // 새 결제 요청
    public PaymentEntity newRequest(Long orderId, Long userId, Long amount, PaymentMethod method, Long couponId) {
        return PaymentEntity.request(orderId, userId, amount, method, couponId);
    }

    // PG 트랜잭션 접수 후 PENDING
    public void markPending(PaymentEntity payment, String pgTxId) {
        if (payment.getStatus() == PaymentStatus.APPROVED || payment.getStatus() == PaymentStatus.PENDING) return;
        payment.markPending(pgTxId);
    }

    // 승인 확정
    public void confirmApproved(PaymentEntity payment) {
        if (payment.getStatus() == PaymentStatus.APPROVED) return;
        payment.approve();
    }

    // 거절 확정
    public void confirmDeclined(PaymentEntity payment) {
        if (payment.getStatus() == PaymentStatus.DECLINED) return;
        payment.decline();
    }

    // 실패 처리
    public void markFailed(PaymentEntity payment) {
        if (payment.getStatus() == PaymentStatus.APPROVED) return;
        payment.fail();
    }
}

