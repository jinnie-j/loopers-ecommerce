package com.loopers.domain.order;

import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public OrderInfo createOrder(OrderCommand.Order orderCommand) {
        List<OrderItemEntity> items = orderCommand.orderItems().stream()
                .map(item -> OrderItemEntity.of(item.productId(), item.quantity(), item.price()))
                .toList();
        OrderEntity orderEntity = OrderEntity.of(orderCommand.userId(), items);
        OrderEntity saved = orderRepository.save(orderEntity);

        long totalAmount = orderCommand.orderItems().stream()
                .mapToLong(i -> i.price() * i.quantity())
                .sum();

        publisher.publishEvent(OrderCreatedEvent.of(
                saved.getId(),
                orderCommand.userId(),
                totalAmount,
                orderCommand.couponId()
        ));

        return OrderInfo.from(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderInfo> getOrders(Long userId) {
        var orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(OrderInfo::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderInfo getOrder(Long orderId) {
        OrderEntity orderEntity = orderRepository.findById(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));

        return OrderInfo.from(orderEntity);
    }

    @Transactional
    public OrderEntity save(OrderEntity order) {
        return orderRepository.save(order);
    }
}
