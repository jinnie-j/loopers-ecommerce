package com.loopers.interfaces.api.order;

import com.loopers.domain.order.OrderInfo;

import java.util.List;

public class OrderResponse {

    public record Detail(
            Long orderId,
            Long userId,
            Long totalPrice,
            List<OrderInfo.OrderItemInfo> orderItems
    ) {
        public static Detail from(OrderInfo orderInfo) {
            List<OrderItem> items = orderInfo.orderItems().stream()
                    .map(OrderItem::from)
                    .toList();

            return new Detail(
                    orderInfo.id(),
                    orderInfo.userId(),
                    orderInfo.totalPrice(),
                    orderInfo.orderItems()
            );
        }
    }

    public record OrderItem(
            Long productId,
            Long quantity,
            Long price
    ) {
        public static OrderItem from(OrderInfo.OrderItemInfo info) {
            return new OrderItem(
                    info.productId(),
                    info.quantity(),
                    info.price()
            );
        }
    }
}
