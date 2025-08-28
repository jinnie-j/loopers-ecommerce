package com.loopers.application.order;

import com.loopers.application.payment.PaymentCriteria;
import com.loopers.application.payment.PaymentFacade;
import com.loopers.domain.coupon.CouponEntity;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.*;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.userCoupon.UserCouponCommand;
import com.loopers.domain.userCoupon.UserCouponService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final CouponService couponService;
    private final PaymentFacade paymentFacade;
    private final ProductService productService;

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

        // 결제 요청
        paymentFacade.requestPayment(new PaymentCriteria.RequestPayment(
                saved.getId(),
                c.userId(),
                payable,
                PaymentMethod.CARD,
                new PaymentCriteria.RequestPayment.Card(c.cardType(), c.cardNo()),
                c.couponId()
        ));

        return OrderInfo.from(saved);
    }
}
