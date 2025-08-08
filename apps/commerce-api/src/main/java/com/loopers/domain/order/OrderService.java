package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderInfo createOrder(OrderCommand.Order orderCommand) {
        List<OrderItemEntity> items = orderCommand.orderItems().stream()
                .map(item -> OrderItemEntity.of(item.productId(), item.quantity(), item.price()))
                .toList();
        OrderEntity orderEntity = OrderEntity.of(orderCommand.userId(), items);
        OrderEntity saved = orderRepository.save(orderEntity);
        return OrderInfo.from(saved);
    }

    public List<OrderInfo> getOrders(Long userId) {
        var orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(OrderInfo::from)
                .toList();
    }

    public OrderInfo getOrder(Long orderId) {
        OrderEntity orderEntity = orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));

        return OrderInfo.from(orderEntity);
    }
}
