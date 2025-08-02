package com.loopers.domain.order;


import java.util.List;

public record OrderInfo(
        Long id,
        Long userId,
        Long totalPrice,
        List<OrderItemInfo> orderItems
) {
    public static OrderInfo from(OrderEntity entity) {
        return new OrderInfo(
                entity.getId(),
                entity.getUserId(),
                entity.getTotalPrice(),
                entity.getOrderItems().stream()
                        .map(OrderItemInfo::from)
                        .toList()
        );
    }

    public record OrderItemInfo(
            Long productId,
            Long quantity,
            Long price
    ) {
        public static OrderItemInfo from(OrderItemEntity entity) {
            return new OrderItemInfo(
                    entity.getProductId(),
                    entity.getQuantity(),
                    entity.getPrice()
            );
        }
    }
}
