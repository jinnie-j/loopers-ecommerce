package com.loopers.application.order;

import com.loopers.domain.coupon.CouponEntity;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.*;
import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final CouponService couponService;
    private final ProductService productService;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public OrderInfo createOrder(OrderCriteria.CreateWithPayment c) {

        productService.validateAvailability(c.orderItems());
        // 주문 저장
        List<OrderItemEntity> items = c.orderItems().stream()
                .map(i -> OrderItemEntity.of(i.productId(), i.quantity(), i.price()))
                .toList();

        OrderEntity order = OrderEntity.of(c.userId(), items);
        OrderEntity saved = orderService.save(order);

        long totalPrice = saved.getTotalPrice();

        // 할인 계산(검증)
        long discount = 0L;
        if (c.couponId() != null) {
            CouponEntity coupon = couponService.getAvailableCoupon(c.couponId());
            discount = couponService.applyDiscount(coupon, totalPrice);
        }
        long payable = Math.max(0, totalPrice - discount);

        publisher.publishEvent(new OrderCreatedEvent(
                saved.getId(),
                c.userId(),
                payable,
                c.couponId(),
                PaymentMethod.CARD,
                c.cardType(),
                c.cardNo()
        ));

        return OrderInfo.from(saved);
    }
}
