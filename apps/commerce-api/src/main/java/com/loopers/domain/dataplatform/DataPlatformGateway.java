package com.loopers.domain.dataplatform;

public interface DataPlatformGateway {
    void sendOrderData(Long orderId);
    void sendPaymentResult(Long orderId, String status);
}
