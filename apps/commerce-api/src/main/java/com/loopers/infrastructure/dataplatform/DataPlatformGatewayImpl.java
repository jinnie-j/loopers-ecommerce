package com.loopers.infrastructure.dataplatform;

import com.loopers.domain.dataplatform.DataPlatformGateway;
import org.springframework.stereotype.Component;

@Component
public class DataPlatformGatewayImpl implements DataPlatformGateway {
    @Override
    public void sendOrderData(Long orderId) {

    }

    @Override
    public void sendPaymentResult(Long orderId, String status) {

    }
}
