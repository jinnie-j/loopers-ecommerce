package com.loopers.application.order.event;

import com.loopers.application.payment.PaymentCriteria;
import com.loopers.application.payment.PaymentFacade;
import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.domain.point.PointService;
import com.loopers.domain.userCoupon.UserCouponCommand;
import com.loopers.domain.userCoupon.UserCouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderCreatedEventHandler {

    private final UserCouponService userCouponService;
    private final PointService pointService;
    private final PaymentFacade paymentFacade; // 외부 I/O 호출


    @Async("appExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OrderCreatedEvent e) {
        // 1) 쿠폰 차감/예약
        useCouponIfPresent(e);
        // 2) 포인트 적립/기록
        recordPoint(e);
        // 3) PG 결제 요청
        requestPayment(e);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void useCouponIfPresent(OrderCreatedEvent e) {
        if (e.couponId() != null) {
            userCouponService.useCoupon(new UserCouponCommand.Use(e.userId(), e.couponId()));
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void recordPoint(OrderCreatedEvent e) {
        if (e.totalAmount() > 0) {
            pointService.usePoints(e.userId(), e.totalAmount());
        }
    }

    void requestPayment(OrderCreatedEvent e) {
        paymentFacade.requestPayment(new PaymentCriteria.RequestPayment(
                e.orderId(),
                e.userId(),
                e.totalAmount(),
                e.method(),
                new PaymentCriteria.RequestPayment.Card(e.cardType(), e.cardNo()),
                e.couponId()
        ));
    }
}
