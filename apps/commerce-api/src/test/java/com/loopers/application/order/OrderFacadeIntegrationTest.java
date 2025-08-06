package com.loopers.application.order;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderEntity;
import com.loopers.domain.order.OrderInfo;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.point.PointEntity;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("OrderFacade 통합 테스트")
@SpringBootTest
class OrderFacadeIntegrationTest {

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private OrderRepository orderRepository;


    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("정상적인 주문 시 재고, 포인트가 차감되고 주문이 생성된다")
    void createOrder_success() {
        // arrange
        long userId = 1L;
        BrandEntity brand = brandRepository.save(BrandEntity.of("Apple", "애플 브랜드"));
        ProductEntity product = productRepository.save(
                new ProductEntity("맥북", 3_000_000L, 10L, brand.getId())
        );
        pointRepository.save(new PointEntity(userId, 5_000_000L));

        OrderCommand.Order command = new OrderCommand.Order(
                userId,
                List.of(new OrderCommand.OrderItem(product.getId(), 1L, 3_000_000L))
        );

        // act
        OrderInfo result = orderFacade.createOrder(command);

        // assert
        ProductEntity updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        PointEntity updatedPoint = pointRepository.findByUserId(userId).orElseThrow();

        assertThat(result.totalPrice()).isEqualTo(3_000_000L);
        assertThat(updatedProduct.getStock()).isEqualTo(9L);
        assertThat(updatedPoint.getBalance()).isEqualTo(2_000_000L);
    }

    @Test
    @DisplayName("재고가 부족하면 주문이 실패한다")
    void createOrder_stockInsufficient() {
        // arrange
        long userId = 1L;
        BrandEntity brand = brandRepository.save(BrandEntity.of("Apple", "애플 브랜드"));
        ProductEntity product = productRepository.save(
                new ProductEntity("맥북", 3_000_000L, 0L, brand.getId())
        );
        pointRepository.save(new PointEntity(userId, 5_000_000L));

        OrderCommand.Order command = new OrderCommand.Order(
                userId,
                List.of(new OrderCommand.OrderItem(product.getId(), 1L, 3_000_000L))
        );

        // act & assert
        CoreException exception = assertThrows(CoreException.class, () -> {
            orderFacade.createOrder(command);
        });

        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @Test
    @DisplayName("포인트가 부족하면 주문이 실패한다")
    void createOrder_pointInsufficient() {
        // arrange
        long userId = 1L;
        BrandEntity brand = brandRepository.save(BrandEntity.of("Apple", "애플 브랜드"));
        ProductEntity product = productRepository.save(
                new ProductEntity("맥북", 3_000_000L, 10L, brand.getId())
        );
        pointRepository.save(new PointEntity(userId, 1_000_000L));

        OrderCommand.Order command = new OrderCommand.Order(
                userId,
                List.of(new OrderCommand.OrderItem(product.getId(), 1L, 3_000_000L))
        );

        // act & assert
        CoreException exception = assertThrows(CoreException.class, () -> {
            orderFacade.createOrder(command);
        });

        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }

    @Test
    @DisplayName("동시에 10개의 주문이 들어와도 재고는 100 -> 0 으로 정확히 차감된다.")
    void concurrentOrders_reduceStock() throws InterruptedException {

        Long userId = 1L;
        BrandEntity brand = brandRepository.save(BrandEntity.of("브랜드", "설명"));

        // 상품 초기 재고 100개
        ProductEntity product = ProductEntity.of("상품", 1000L, 100L, brand.getId());
        Long productId = productRepository.save(product).getId();

        // 사용자 포인트 세팅 (총 10명 * 10개 = 100개 * 1000원)
        pointRepository.save(new PointEntity(userId, 100_000));

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.execute(() -> {
                try {
                    OrderCommand.OrderItem item = new OrderCommand.OrderItem(productId, 10L, 1000L);
                    OrderCommand.Order command = new OrderCommand.Order(userId, List.of(item));
                    orderFacade.createOrder(command);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // 상품 재고 확인
        ProductEntity updated = productRepository.findById(productId).orElseThrow();
        assertThat(updated.getStock()).isEqualTo(0L);

        // 주문 10개가 생성되었는지 확인
        List<OrderEntity> orders = orderRepository.findByUserId(userId);
        assertThat(orders).hasSize(threadCount);
    }
}
