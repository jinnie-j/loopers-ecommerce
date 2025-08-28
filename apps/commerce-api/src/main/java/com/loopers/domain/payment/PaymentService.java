package com.loopers.domain.payment;

import com.loopers.application.payment.PaymentGateway;
import com.loopers.domain.payment.event.PaymentApprovedEvent;
import com.loopers.domain.payment.event.PaymentDeclinedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGateway gateway;
    private final ApplicationEventPublisher publisher;

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

    @Transactional
    public int reconcilePaymentStatuses(LocalDateTime cutoff, int batchSize) {
        List<PaymentEntity> targets = paymentRepository.findReconTargets(cutoff, batchSize);
        int processed = 0;

        for (PaymentEntity payment : targets) {
            if (payment.getPgTxId() == null) continue;
            try {
                var dto = gateway.getPaymentByTx(payment.getPgTxId());
                if (dto == null || dto.status() == null) continue;

                switch (dto.status()) {
                    case "APPROVED" -> {
                        if (payment.getStatus() != PaymentStatus.APPROVED) {
                            confirmApproved(payment); // 상태 변경
                            publisher.publishEvent(new PaymentApprovedEvent(
                                    payment.getOrderId(), payment.getUserId(), payment.getAmount(), payment.getCouponId()
                            ));
                        }
                        processed++;
                    }
                    case "DECLINED", "CANCELED", "EXPIRED" -> {
                        if (payment.getStatus() != PaymentStatus.DECLINED) {
                            confirmDeclined(payment);
                            publisher.publishEvent(new PaymentDeclinedEvent(payment.getOrderId()));
                        }
                        processed++;
                    }
                    default -> log.debug("pending txId={} id={} status={}",
                            payment.getPgTxId(), payment.getId(), dto.status());
                }
            } catch (Exception e) {
                log.warn("recon error txId={} id={}: {}", payment.getPgTxId(), payment.getId(), e.toString());
            }
        }
        return processed;
    }
}

