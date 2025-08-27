package com.loopers.application.dataplatform;

import com.loopers.application.payment.PaymentApprovedEvent;
import com.loopers.application.payment.PaymentDeclinedEvent;
import com.loopers.domain.dataplatform.DataPlatformGateway;
import com.loopers.domain.order.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class DataPlatformEventHandler {

    private final DataPlatformGateway dataPlatformGateway;

    @Async("appExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent e) {
        dataPlatformGateway.sendOrderData(e.orderId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentApproved(PaymentApprovedEvent e) {
        dataPlatformGateway.sendPaymentResult(e.orderId(), "APPROVED");
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentDeclined(PaymentDeclinedEvent e) {
        dataPlatformGateway.sendPaymentResult(e.orderId(), "DECLINED");
    }
}
