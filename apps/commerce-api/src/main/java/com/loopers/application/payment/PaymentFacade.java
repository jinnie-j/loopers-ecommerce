package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentFacade {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final PaymentGateway gateway;
    private final ApplicationEventPublisher publisher;

    @Value("${pg.callback-url}") private String callbackUrl;

    @Transactional
    public PaymentResult.Summary requestPayment(PaymentCriteria.RequestPayment requestPayment) {
        PaymentEntity payment = paymentRepository.findByOrderId(requestPayment.orderId())
                .orElseGet(() -> paymentRepository.save(
                        PaymentEntity.request(requestPayment.orderId(), requestPayment.userId(), requestPayment.payableAmount(), requestPayment.method(), requestPayment.couponId())
                ));

        if (payment.getStatus() == PaymentStatus.APPROVED || payment.getStatus() == PaymentStatus.PENDING) {
            return PaymentResult.Summary.from(payment);
        }

        var card = requestPayment.card();
        var response = gateway.requestPayment(new PaymentGateway.CreatePaymentRequest(
                String.valueOf(requestPayment.orderId()),
                requestPayment.payableAmount(),
                callbackUrl,
                requestPayment.method(),
                card != null ? card.cardType() : null,
                card != null ? card.cardNo()   : null
        ));

        if (response != null && response.transactionId() != null) payment.markPending(response.transactionId());
        else log.warn("PG create deferred orderId={} (resStatus={}, txId=null)",
                requestPayment.orderId(), (response != null ? response.status() : "null"));

        return PaymentResult.Summary.from(payment);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processPgCallback(PaymentCriteria.ProcessPgCallback pgCallback) {
        PaymentEntity payment = paymentRepository.findByPgTxId(pgCallback.transactionId())
                .orElseThrow(() -> new IllegalArgumentException("unknown txId: " + pgCallback.transactionId()));

        if (payment.getStatus() == PaymentStatus.APPROVED || payment.getStatus() == PaymentStatus.DECLINED) return;

        switch (pgCallback.status()) {
            case "APPROVED" -> {
                paymentService.confirmApproved(payment);
                publisher.publishEvent(new PaymentApprovedEvent(payment.getOrderId(), payment.getUserId(), payment.getAmount(), payment.getCouponId()));
            }
            case "DECLINED" -> {
                paymentService.confirmDeclined(payment);
                publisher.publishEvent(new PaymentDeclinedEvent(payment.getOrderId()));
            }
            default -> paymentService.markFailed(payment);
        }
    }
}
