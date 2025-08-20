package com.loopers.application.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderCriteria {

    //결제 주문 생성 DTO
    public record CreateWithPayment(
            Long userId,
            List<Item> orderItems,
            Long couponId,
            String cardType,
            String cardNo
    ) {
        public record Item(Long productId, Long quantity, Long price) {}
    }
}
