package com.loopers.application.payment.event;

import com.loopers.application.payment.PaymentApprovedEvent;
import com.loopers.application.payment.PaymentDeclinedEvent;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentEventHandler {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final PointService pointService;
    private final UserCouponService userCouponService;

    @Async("appExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onApproved(PaymentApprovedEvent e) {
        OrderEntity order = orderRepository.findById(e.orderId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));
        if (order.isTerminal()) return;

        // 최종 확정: 재고/포인트/쿠폰
        for (OrderItemEntity item : order.getOrderItems()) {
            productService.decreaseStock(item.getProductId(), item.getQuantity());
        }
        if (e.payableAmount() > 0) {
            pointService.usePoints(e.userId(), e.payableAmount());
        }
        if (e.couponId() != null) {
            userCouponService.useCoupon(new UserCouponCommand.Use(e.userId(), e.couponId()));
        }

        order.markPaid();
    }

    @Async("appExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onDeclined(PaymentDeclinedEvent e) {
        OrderEntity order = orderRepository.findById(e.orderId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));
        if (order.isTerminal()) return;
        order.markPaymentFailed();
    }
}
