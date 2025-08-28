package com.loopers.application.order;

import com.loopers.application.payment.PaymentCriteria;
import com.loopers.application.payment.PaymentFacade;
import com.loopers.application.payment.PaymentGateway;
import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.order.*;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.point.PointEntity;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.utils.DatabaseCleanUp;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

@DisplayName("OrderFacade 통합 테스트")
@SpringBootTest(properties = {
        "pg.base-url=http://localhost:9999",
        "pg.user-id=135135",
        "pg.callback-url=http://localhost:8080/api/v1/payments/callback",
        "payments.recon.batch-size=50",
        "payments.recon.fixed-delay=10s"
})
class OrderFacadeIntegrationTest {

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private PaymentFacade paymentFacade;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockitoBean private PaymentGateway gateway;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @PersistenceContext
    EntityManager em;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("주문 생성 시 결제 요청이 되고, 재고/포인트는 차감되지 않는다.")
    void createOrder_success() {
        // arrange
        long userId = 1L;
        BrandEntity brand = brandRepository.save(BrandEntity.of("Apple", "애플 브랜드"));
        ProductEntity product = productRepository.save(
                ProductEntity.of("맥북", 3_000_000L, 10L, brand.getId())
        );
        pointRepository.save(new PointEntity(userId, 5_000_000L));

        when(gateway.requestPayment(any())).thenReturn(
                new PaymentGateway.CreatePaymentResponse("TX-123", "ACCEPTED")
        );

        var req = new OrderCriteria.CreateWithPayment(
                userId,
                List.of(new OrderCriteria.CreateWithPayment.Item(
                        product.getId(), 1L, 3_000_000L
                )),
                null, "SAMSUNG", "1234-5678-9012-3456"
        );

        var result = orderFacade.createOrder(req);

        awaitPaymentPending(result.id(), "TX-123");

        var updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        var updatedPoint = pointRepository.findByUserId(userId).orElseThrow();
        var payment = paymentRepository.findByOrderId(result.id()).orElseThrow();

        assertThat(result.totalPrice()).isEqualTo(3_000_000L);
        assertThat(updatedProduct.getStock()).isEqualTo(10L);        // 차감 X
        assertThat(updatedPoint.getBalance()).isEqualTo(5_000_000L); // 차감 X
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getPgTxId()).isEqualTo("TX-123");
    }

    @Test
    @DisplayName("PG 승인 콜백이 오면 재고/포인트가 차감되고 주문 상태가 PAID로 변한다.")
    void approveCallback_shouldDeductAndMarkPaid() {
        // arrange
        long userId = 1L;
        BrandEntity brand = brandRepository.save(BrandEntity.of("Apple", "애플"));
        ProductEntity product = productRepository.save(ProductEntity.of("맥북", 3_000_000L, 10L, brand.getId()));
        pointRepository.save(new PointEntity(userId, 5_000_000L));

        when(gateway.requestPayment(any())).thenReturn(
                new PaymentGateway.CreatePaymentResponse("TX-APPROVE", "ACCEPTED")
        );

        var req = new OrderCriteria.CreateWithPayment(
                userId,
                List.of(new OrderCriteria.CreateWithPayment.Item(
                        product.getId(), 1L, 3_000_000L)),
                null, "SAMSUNG", "1234-5678-9012-3456"
        );
        OrderInfo order = orderFacade.createOrder(req);

        awaitPaymentPending(order.id(), "TX-APPROVE");

        // act: 승인 콜백
        paymentFacade.processPgCallback(
                new PaymentCriteria.ProcessPgCallback(
                        "TX-APPROVE", "APPROVED", null
                )
        );
        em.clear();
        // assert: 차감/상태 전환 확인
        ProductEntity updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        PointEntity updatedPoint = pointRepository.findByUserId(userId).orElseThrow();
        OrderEntity updatedOrder = orderRepository.findById(order.id()).orElseThrow();

        assertThat(updatedProduct.getStock()).isEqualTo(9L);
        assertThat(updatedPoint.getBalance()).isEqualTo(2_000_000L);
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    @DisplayName("PG 거절 콜백이 오면 주문 상태는 PAYMENT_FAILED가 되고 차감은 없다")
    void declineCallback_shouldFailOrder_noDeduction() {
        long userId = 1L;
        BrandEntity brand = brandRepository.save(BrandEntity.of("Apple", "애플"));
        ProductEntity product = productRepository.save(ProductEntity.of("맥북", 3_000_000L, 10L, brand.getId()));
        pointRepository.save(new PointEntity(userId, 5_000_000L));

        when(gateway.requestPayment(any())).thenReturn(
                new PaymentGateway.CreatePaymentResponse("TX-DECLINE", "ACCEPTED")
        );

        var req = new OrderCriteria.CreateWithPayment(
                userId,
                List.of(new OrderCriteria.CreateWithPayment.Item(
                        product.getId(), 1L, 3_000_000L)),
                null, "SAMSUNG", "1234-5678-9012-3456"
        );
        OrderInfo order = orderFacade.createOrder(req);
        awaitPaymentPending(order.id(), "TX-DECLINE");
        // act
        paymentFacade.processPgCallback(
                new PaymentCriteria.ProcessPgCallback(
                        "TX-DECLINE", "DECLINED", null
                )
        );

        // assert
        ProductEntity updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        PointEntity updatedPoint = pointRepository.findByUserId(userId).orElseThrow();
        OrderEntity updatedOrder = orderRepository.findById(order.id()).orElseThrow();

        assertThat(updatedProduct.getStock()).isEqualTo(10L); // 차감 X
        assertThat(updatedPoint.getBalance()).isEqualTo(5_000_000L);
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);
    }


    @Test
    @DisplayName("승인 콜백이 중복으로 와도(멱등) 재고/포인트는 한 번만 차감된다")
    void callbackIdempotent() {
        long userId = 1L;
        BrandEntity brand = brandRepository.save(BrandEntity.of("Apple", "애플"));
        ProductEntity product = productRepository.save(ProductEntity.of("맥북", 3_000_000L, 10L, brand.getId()));
        pointRepository.save(new PointEntity(userId, 5_000_000L));

        when(gateway.requestPayment(any())).thenReturn(
                new PaymentGateway.CreatePaymentResponse("TX-IDEMP", "ACCEPTED")
        );

        var req = new OrderCriteria.CreateWithPayment(
                userId,
                List.of(new OrderCriteria.CreateWithPayment.Item(
                        product.getId(), 1L, 3_000_000L)),
                null, "SAMSUNG", "1234-5678-9012-3456"
        );
        OrderInfo order = orderFacade.createOrder(req);

        awaitPaymentPending(order.id(), "TX-IDEMP");

        // 승인 콜백 2번
        var cb = new PaymentCriteria.ProcessPgCallback("TX-IDEMP", "APPROVED", null);
        paymentFacade.processPgCallback(cb);
        paymentFacade.processPgCallback(cb);

        ProductEntity updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        PointEntity updatedPoint = pointRepository.findByUserId(userId).orElseThrow();

        assertThat(updatedProduct.getStock()).isEqualTo(9L);         // 한 번만 차감
        assertThat(updatedPoint.getBalance()).isEqualTo(2_000_000L); // 한 번만 차감
    }

    private void awaitPaymentPending(Long orderId, String expectedTx) {
        await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> {
            var p = paymentRepository.findByOrderId(orderId).orElseThrow();
            assertThat(p.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(p.getPgTxId()).isEqualTo(expectedTx);
        });
    }
}
