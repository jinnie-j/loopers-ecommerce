package com.loopers.interfaces.api.order;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Order API")
@RequestMapping("/api/v1/orders")
public interface OrderV1ApiSpec {

    @PostMapping
    @Operation(summary = "주문 생성")
    ApiResponse<OrderResponse.Detail> createOrder(
            @RequestHeader("X-USER-ID") Long userId, @RequestBody OrderRequest.Create orderRequest);

    @Operation(summary = "사용자의 주문 목록 조회")
    @GetMapping
    ApiResponse<List<OrderResponse.Detail>> getOrders(
            @RequestHeader("X-USER-ID") Long userId
    );

    @Operation(summary = "주문 상세 조회")
    @GetMapping("/{orderId}")
    ApiResponse<OrderResponse.Detail> getOrder(@PathVariable Long orderId);

}
