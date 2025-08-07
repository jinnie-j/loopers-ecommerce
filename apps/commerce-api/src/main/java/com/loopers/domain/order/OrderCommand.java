package com.loopers.domain.order;

import java.util.List;

public class OrderCommand {
    public record Order(
            Long userId, List<OrderItem> orderItems, Long couponId
    ){}

    public record OrderItem(
        Long productId, Long quantity, Long price
    ){}
}
