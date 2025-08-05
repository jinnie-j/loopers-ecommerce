package com.loopers.interfaces.api.order;

import com.loopers.domain.order.OrderCommand;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class OrderRequest {
    public record Create(
            @NotNull List<OrderCommand.OrderItem> orderItems
    ) {
        public OrderCommand.Order toCommand(Long userId) {
            List<OrderCommand.OrderItem> items = orderItems.stream()
                    .map(item -> new OrderCommand.OrderItem(item.productId(), item.quantity(), item.price()))
                    .toList();

            return new OrderCommand.Order(userId, items);
        }
    }

}
