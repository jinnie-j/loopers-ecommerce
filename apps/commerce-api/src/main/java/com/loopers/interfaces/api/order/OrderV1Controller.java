package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderFacade;
import com.loopers.domain.order.OrderInfo;
import com.loopers.domain.order.OrderService;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderV1Controller implements OrderV1ApiSpec {

    private final OrderService orderService;
    private final OrderFacade orderFacade;

    @Override
    public ApiResponse<OrderResponse.Detail> createOrder(Long userId, OrderRequest.Create orderRequest) {
        OrderInfo orderInfo = orderFacade.createOrder(orderRequest.toCommand(userId));
        return ApiResponse.success(OrderResponse.Detail.from(orderInfo));
    }

    @Override
    public ApiResponse<List<OrderResponse.Detail>> getOrders(Long userId) {
        List<OrderInfo> orderInfos = orderService.getOrders(userId);
        List<OrderResponse.Detail> response = orderInfos.stream()
                .map(OrderResponse.Detail::from)
                .toList();
        return ApiResponse.success(response);
    }

    @Override
    public ApiResponse<OrderResponse.Detail> getOrder(Long orderId) {
        OrderInfo orderInfo = orderService.getOrder(orderId);
        return ApiResponse.success(OrderResponse.Detail.from(orderInfo));
    }
}
