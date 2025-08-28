package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.payment.event.PaymentApprovedEvent;
import com.loopers.domain.payment.event.PaymentDeclinedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentScheduler {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final PaymentGateway gateway;
    private final ApplicationEventPublisher publisher;

    @Value("${payments.recon.batch-size}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${payments.recon.fixed-delay}")
    @Transactional
    public void run() {

        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(10);
        List<PaymentEntity> targets = paymentRepository.findReconTargets(cutoff, batchSize);

        for (PaymentEntity payment : targets) {
            if (payment.getPgTxId() == null) continue;
            try {
                var dto = gateway.getPaymentByTx(payment.getPgTxId());
                if (dto == null || dto.status() == null) continue;

                switch (dto.status()) {
                    case "APPROVED" -> {
                        if (payment.getStatus() != PaymentStatus.APPROVED) {
                            paymentService.confirmApproved(payment);
                            publisher.publishEvent(new PaymentApprovedEvent(
                                    payment.getOrderId(), payment.getUserId(), payment.getAmount(), payment.getCouponId()
                            ));
                        }
                    }
                    case "DECLINED", "CANCELED", "EXPIRED" -> {
                        if (payment.getStatus() != PaymentStatus.DECLINED) {
                            paymentService.confirmDeclined(payment);
                            publisher.publishEvent(new PaymentDeclinedEvent(payment.getOrderId()));
                        }
                    }
                    default -> {
                        log.debug("pending txId={} id={} status={}", payment.getPgTxId(), payment.getId(), dto.status());
                    }
                }
            } catch (Exception e) {
                log.warn("error txId={} id={}: {}", payment.getPgTxId(), payment.getId(), e.toString());
            }
        }
    }
}
