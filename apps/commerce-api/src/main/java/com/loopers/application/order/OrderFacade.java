package com.loopers.application.order;

import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderInfo;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final ProductService productService;
    private final PointService pointService;

    @Transactional
    public OrderInfo createOrder(OrderCommand.Order command) {

        command.orderItems().forEach(item ->
                productService.decreaseStock(item.productId(), item.quantity()));

        Long totalPrice = command.orderItems().stream()
                .mapToLong(item -> item.price() * item.quantity())
                .sum();

        pointService.usePoints(command.userId(), totalPrice);

        return orderService.createOrder(command);
    }
}
