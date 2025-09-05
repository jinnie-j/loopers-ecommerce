package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentScheduler {

    private final PaymentService paymentService;

    @Value("${payments.recon.batch-size}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${payments.recon.fixed-delay}")
    @Transactional
    public void run() {

        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(10);
        int count = paymentService.reconcilePaymentStatuses(cutoff, batchSize);
        log.debug("schedule processed={}", count);
    }
}
