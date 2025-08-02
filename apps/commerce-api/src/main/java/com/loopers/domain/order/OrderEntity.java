package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
public class OrderEntity extends BaseEntity {
    @Column(nullable = false)
    private Long userId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<OrderItemEntity> orderItems = new ArrayList<>();

    private Long totalPrice;

    protected OrderEntity() {}

    private OrderEntity(Long userId, List<OrderItemEntity> items) {
        if (userId == null || items == null || items.isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }

        this.userId = userId;
        this.totalPrice = items.stream()
                .mapToLong(OrderItemEntity::calculateItemTotal)
                .sum();

        items.forEach(item -> item.setOrder(this));
        this.orderItems.addAll(items);
    }

    public static OrderEntity of(Long userId, List<OrderItemEntity> items) {
        return new OrderEntity(userId, items);
    }
}
