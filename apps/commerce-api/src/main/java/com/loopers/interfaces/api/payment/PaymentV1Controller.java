package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentFacade;
import com.loopers.application.payment.PaymentResult;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class PaymentV1Controller implements PaymentV1ApiSpec {

    private final PaymentFacade paymentFacade;
    private final PaymentRepository paymentRepository;

    @Override
    public ApiResponse<PaymentResponse.Summary> request(Long userId, @Valid PaymentRequest.Create request) {
        var summary = paymentFacade.requestPayment(request.toCriteria(userId));
        return ApiResponse.success(PaymentResponse.Summary.from(summary));
    }

    @Override
    public ApiResponse<Void> callback(@Valid PaymentRequest.Callback request) {
        paymentFacade.processPgCallback(request.toCriteria());
        return ApiResponse.success(null);
    }

    @Override
    public ApiResponse<PaymentResponse.Summary> getByOrder(Long orderId) {
        var summary = paymentRepository.findByOrderId(orderId)
                .map(PaymentResult.Summary::from)
                .orElse(null);

        return (summary == null)
                ? ApiResponse.success(null)
                : ApiResponse.success(PaymentResponse.Summary.from(summary));
    }
}
