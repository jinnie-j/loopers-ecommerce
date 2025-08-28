package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderCriteria;

import java.util.List;

public class OrderRequest {

    public record Create(
            List<Item> items,
            Long couponId,
            String cardType,
            String cardNo
    ) {
        public OrderCriteria.CreateWithPayment toCommand(Long userId) {
            return new OrderCriteria.CreateWithPayment(
                    userId,
                    items.stream()
                            .map(i -> new OrderCriteria.CreateWithPayment.Item(
                                    i.productId(), i.quantity(), i.price()))
                            .toList(),
                    couponId,
                    cardType,
                    cardNo
            );
        }
    }

    public record Item(Long productId, Long quantity, Long price) {}
}
