package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "order_items")
@Getter
public class OrderItemEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private OrderEntity order;

    private Long productId;
    private Long quantity;
    private Long price;

    protected OrderItemEntity() {}

    private OrderItemEntity(Long productId, Long quantity, Long price) {
        if (productId == null || quantity == null || quantity <= 0 || price == null || price < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    public static OrderItemEntity of(Long productId, Long quantity, Long price) {
        return new OrderItemEntity(productId, quantity, price);
    }

    public long calculateItemTotal() {
        return quantity * price;
    }

    protected void setOrder(OrderEntity order) {
        this.order = order;
    }
}
