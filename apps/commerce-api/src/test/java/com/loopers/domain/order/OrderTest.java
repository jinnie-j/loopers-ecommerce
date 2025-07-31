package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

public class OrderTest {

    @Test
    @DisplayName("주문이 정상적으로 생성된다.")
    void createOrder_success() {
        // arrange
        Long userId = 1L;
        OrderItemEntity item1 = OrderItemEntity.of(100L, 2L, 5000L); 
        OrderItemEntity item2 = OrderItemEntity.of(200L, 1L, 15000L);

        // act
        OrderEntity order = OrderEntity.of(userId, List.of(item1, item2));

        // assert
        assertThat(order).isNotNull();
        assertThat(order.getUserId()).isEqualTo(userId);
        assertThat(order.getOrderItems()).hasSize(2);
        assertThat(order.getTotalPrice()).isEqualTo(25000L);
    }

    @Test
    @DisplayName("상품 목록이 비어있으면 BAD_REQUEST를 반환한다.")
    void fail_whenOrderItemIsNull() {
        // arrange
        Long userId = 1L;

        // act
        CoreException exception = assertThrows(CoreException.class, () ->
                OrderEntity.of(userId, List.of())
        );

        // assert
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @Test
    @DisplayName("유저 ID가 null이면 BAD_REQUEST를 반환한다.")
    void fail_whenOrderItemIsEmpty() {
        // arrange
        OrderItemEntity item = OrderItemEntity.of(100L, 1L, 10000L);

        // act
        CoreException exception = assertThrows(CoreException.class, () ->
                OrderEntity.of(null, List.of(item))
        );

        // assert
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @Test
    @DisplayName("상품 수량이 0 이하면 BAD_REQUEST를 반환한다.")
    void createOrder_fail_invalidQuantity() {
        // act
        CoreException exception = assertThrows(CoreException.class, () ->
                OrderItemEntity.of(100L, 0L, 10000L)
        );

        //assert
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }
}
