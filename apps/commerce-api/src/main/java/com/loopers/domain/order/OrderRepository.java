package com.loopers.domain.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    OrderEntity save(OrderEntity orderEntity);

    List<OrderEntity> findByUserId(Long userId);

    Optional<OrderEntity> findById(Long orderId);
}
