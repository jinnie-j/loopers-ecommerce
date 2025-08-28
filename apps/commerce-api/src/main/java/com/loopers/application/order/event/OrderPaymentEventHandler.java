package com.loopers.application.order.event;

import com.loopers.domain.payment.event.PaymentApprovedEvent;
import com.loopers.domain.payment.event.PaymentDeclinedEvent;
import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.order.OrderItemEntity;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.userCoupon.UserCouponCommand;
import com.loopers.domain.userCoupon.UserCouponService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderPaymentEventHandler {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final PointService pointService;
    private final UserCouponService userCouponService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onApproved(PaymentApprovedEvent e) {
        OrderEntity order = orderRepository.findById(e.orderId()).orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));
        if (order.isTerminal()) return;

        for (OrderItemEntity item : order.getOrderItems()) productService.decreaseStock(item.getProductId(), item.getQuantity());
        if (e.payableAmount() > 0) pointService.usePoints(e.userId(), e.payableAmount());
        if (e.couponId() != null) userCouponService.useCoupon(new UserCouponCommand.Use(e.userId(), e.couponId()));

        order.markPaid();
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onDeclined(PaymentDeclinedEvent e) {
        OrderEntity order = orderRepository.findById(e.orderId()).orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));
        if (order.isTerminal()) return;
        order.markPaymentFailed();
    }
}
