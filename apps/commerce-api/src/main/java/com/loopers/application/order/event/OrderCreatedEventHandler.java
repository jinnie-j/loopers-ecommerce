package com.loopers.application.order.event;

import com.loopers.application.payment.PaymentCriteria;
import com.loopers.application.payment.PaymentFacade;
import com.loopers.domain.order.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderCreatedEventHandler {


    private final PaymentFacade paymentFacade;

    @Async("appExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent e) {
        paymentFacade.requestPayment(new PaymentCriteria.RequestPayment(
                e.orderId(), e.userId(), e.totalAmount(), e.method(),
                new PaymentCriteria.RequestPayment.Card(e.cardType(), e.cardNo()),
                e.couponId()
        ));
    }
}
