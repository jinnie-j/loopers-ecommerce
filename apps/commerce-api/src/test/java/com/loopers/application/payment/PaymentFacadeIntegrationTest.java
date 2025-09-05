package com.loopers.application.payment;

import com.loopers.application.order.OrderCriteria;
import com.loopers.application.order.OrderFacade;
import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.point.PointEntity;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("PaymentFacade 통합 테스트")
@SpringBootTest(properties = {
        "pg.base-url=http://localhost:9999",
        "pg.user-id=135135",
        "pg.callback-url=http://localhost:8080/api/v1/payments/callback",
        "payments.recon.batch-size=50",
        "payments.recon.fixed-delay=10s"
})
class PaymentFacadeIntegrationTest {

    @Autowired private PaymentFacade paymentFacade;
    @Autowired private OrderFacade orderFacade;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private PointRepository pointRepository;
    @Autowired private BrandRepository brandRepository;

    @MockitoBean
    private PaymentGateway gateway;

    @Autowired private DatabaseCleanUp databaseCleanUp;
    @AfterEach
    void tearDown(){ databaseCleanUp.truncateAllTables(); }

    @Test
    @DisplayName("PG 호출 성공 시 Payment가 PENDING으로 저장된다")
    void createPayment_pending() {
        // 주문 준비
        long userId = 1L;
        BrandEntity brand = brandRepository.save(BrandEntity.of("B", "desc"));
        ProductEntity product = productRepository.save(ProductEntity.of("P", 1000L, 5L, brand.getId()));
        pointRepository.save(new PointEntity(userId, 10_000L));

        when(gateway.requestPayment(any())).thenReturn(
                new PaymentGateway.CreatePaymentResponse("TX-777", "ACCEPTED")
        );

        var req = new OrderCriteria.CreateWithPayment(
                userId, List.of(new OrderCriteria.CreateWithPayment.Item(
                product.getId(), 1L, 1000L)), null, "SAMSUNG", "1111-2222-3333-4444");

        var order = orderFacade.createOrder(req);

        awaitPaymentPending(order.id(), "TX-777");

        var payment = paymentRepository.findByOrderId(order.id()).orElseThrow();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getPgTxId()).isEqualTo("TX-777");
    }

    @Test
    @DisplayName("콜백 APPROVED: 주문/재고/포인트 확정")
    void callbackApproved() {
        long userId = 1L;
        BrandEntity brand = brandRepository.save(BrandEntity.of("B", "desc"));
        ProductEntity product = productRepository.save(ProductEntity.of("P", 1000L, 2L, brand.getId()));
        pointRepository.save(new PointEntity(userId, 5_000L));

        when(gateway.requestPayment(any())).thenReturn(
                new PaymentGateway.CreatePaymentResponse("TX-A", "ACCEPTED")
        );

        var req = new OrderCriteria.CreateWithPayment(
                userId, List.of(new OrderCriteria.CreateWithPayment.Item(
                product.getId(), 1L, 1000L)), null, "SAMSUNG", "1111-2222-3333-4444");
        var order = orderFacade.createOrder(req);

        awaitPaymentPending(order.id(), "TX-A");

        paymentFacade.processPgCallback(
                new PaymentCriteria.ProcessPgCallback("TX-A", "APPROVED", null)
        );

        var payment = paymentRepository.findByOrderId(order.id()).orElseThrow();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);

        var updatedOrder = orderRepository.findById(order.id()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    @DisplayName("콜백 DECLINED: 주문 PAYMENT_FAILED, 차감 없음")
    void callbackDeclined() {
        long userId = 1L;
        BrandEntity brand = brandRepository.save(BrandEntity.of("B", "desc"));
        ProductEntity product = productRepository.save(ProductEntity.of("P", 1000L, 2L, brand.getId()));
        pointRepository.save(new PointEntity(userId, 5_000L));

        when(gateway.requestPayment(any())).thenReturn(
                new PaymentGateway.CreatePaymentResponse("TX-D", "ACCEPTED")
        );

        var req = new OrderCriteria.CreateWithPayment(
                userId, List.of(new OrderCriteria.CreateWithPayment.Item(
                product.getId(), 1L, 1000L)), null, "SAMSUNG", "1111-2222-3333-4444");
        var order = orderFacade.createOrder(req);

        awaitPaymentPending(order.id(), "TX-D");

        paymentFacade.processPgCallback(
                new PaymentCriteria.ProcessPgCallback("TX-D", "DECLINED", null)
        );

        var payment = paymentRepository.findByOrderId(order.id()).orElseThrow();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.DECLINED);

        var updatedOrder = orderRepository.findById(order.id()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);
    }

    private void awaitPaymentPending(Long orderId, String expectedTx) {
        await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> {
            var p = paymentRepository.findByOrderId(orderId).orElseThrow();
            assertThat(p.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(p.getPgTxId()).isEqualTo(expectedTx);
        });
    }

}
