package com.loopers.application.order;

import com.loopers.domain.coupon.CouponEntity;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderInfo;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.userCoupon.UserCouponCommand;
import com.loopers.domain.userCoupon.UserCouponService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final PointService pointService;
    private final UserCouponService userCouponService;
    private final ProductService productService;
    private final CouponService couponService;

    @Transactional
    public OrderInfo createOrder(OrderCommand.Order command) {
        // 총 상품 금액 계산
        long totalPrice = command.orderItems().stream()
                .mapToLong(item -> item.price() * item.quantity())
                .sum();
        // 재고 차감
        command.orderItems().forEach(item ->
                productService.decreaseStock(item.productId(), item.quantity()));

        // 쿠폰 할인 적용 (쿠폰 존재 시)
        long discountedAmount = 0L;
        if (command.couponId() != null) {

            userCouponService.validateOwnedAndUnused(
                    new UserCouponCommand.Use(command.userId(), command.couponId())
            );

            CouponEntity coupon = couponService.getAvailableCoupon(command.couponId());

            discountedAmount = couponService.applyDiscount(coupon, totalPrice);

            userCouponService.useCoupon(
                    new UserCouponCommand.Use(command.userId(), command.couponId())
            );
        }
            // 최종 결제 금액 = 총액 - 할인
            long finalPayablePrice = totalPrice - discountedAmount;
            if (finalPayablePrice < 0) {
                throw new CoreException(ErrorType.BAD_REQUEST, "결제 금액이 0보다 작을 수 없습니다.");
            }
            pointService.usePoints(command.userId(), totalPrice);

            return orderService.createOrder(command);
        }

}
