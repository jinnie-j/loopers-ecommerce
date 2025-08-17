package com.loopers.domain.product;

import com.loopers.application.product.ProductQueryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
class ProductServiceCacheTest {

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", () -> redis.getHost());
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.cache.type", () -> "redis");
    }

    @Autowired
    ProductService productService;

    @MockitoSpyBean
    ProductRepository productRepository;

    @MockitoBean
    ProductQueryRepository productQueryRepository;
    
    @Autowired
    RedisConnectionFactory connectionFactory;

    @AfterEach
    void clearRedis() {
        // 각 테스트 격리: 캐시 비움
        try (var conn = connectionFactory.getConnection()) {
            conn.serverCommands().flushDb();
        }
        // 스파이 카운트 초기화
        reset(productRepository);
    }

    @Test
    @DisplayName("같은 상품 상세를 두 번 조회해도 DB는 1번만 타고, 두번째는 캐시로 응답한다")
    void cacheHitOnSecondCall() {
        // arrange: 테스트용 상품 저장
        ProductEntity saved = productRepository.save(ProductEntity.of("상품", 1000L, 10L, 1L));

        // act: 동일 ID 두 번 조회
        ProductInfo productA = productService.getProduct(saved.getId()); // miss → DB 1회
        ProductInfo productB = productService.getProduct(saved.getId()); // hit  → DB 0회

        // assert: Repository.findById는 1번만 호출되어야 함(두번째는 캐시 히트)
        verify(productRepository, Mockito.times(1)).findById(saved.getId());
        Assertions.assertEquals(productA.id(), productB.id());
    }

    @Test
    @DisplayName("TTL 만료 후에는 다시 DB를 탄다 (테스트 전용 1초 TTL로 오버라이드 권장)")
    void cacheExpiresThenMissAgain() throws Exception {
        // arrange: 테스트용 상품 저장
        ProductEntity saved = productRepository.save(ProductEntity.of("상품", 1000L, 10L, 1L));

        // act: 동일 ID 두 번 조회
        productService.getProduct(saved.getId()); // miss → DB 1회
        reset(productRepository);

        Thread.sleep(java.time.Duration.ofSeconds(62).toMillis());

        productService.getProduct(saved.getId()); // miss → DB 1회

        // assert: Repository.findById 2번 호출되어야 함
        verify(productRepository, Mockito.times(1)).findById(saved.getId());
    }

    @Test
    @DisplayName("decreaseStock 후 상세 캐시 evict → 다음 조회는 DB 재호출")
    void evictOnDecreaseStock_thenNextReadIsMiss() {
        // arrange
        ProductEntity saved = productRepository.save(ProductEntity.of("상품", 1000L, 10L, 1L));
        long id = saved.getId();

        // act
        // 1) 캐시 채우기: miss → put
        ProductInfo before = productService.getProduct(id);

        // 2) 카운터 리셋
        reset(productRepository);

        // 3) 쓰기(재고 차감) → @CacheEvict
        productService.decreaseStock(id, 1L);

        // 4) 다음 상세 조회: evict 되었으니 miss → DB 1회
        ProductInfo after = productService.getProduct(id);

        //assert
        verify(productRepository, Mockito.times(1))
                .findById(id);

        // 재고 확인
        assertThat(after.stock())
                .isEqualTo(before.stock() - 1);
    }

    @Test
    @DisplayName("같은 상품 상세를 두 번 조회해도 DB는 1번만 타고, 두번째는 캐시로 응답한다")
    void cacheHitOnSecondCalld() {
        // arrange
        ProductEntity saved = productRepository.save(ProductEntity.of("상품", 1000L, 10L, 1L));

        // act
        ProductInfo productA = productService.getProduct(saved.getId()); // miss → DB 1회
        ProductInfo productB = productService.getProduct(saved.getId()); // hit  → DB 0회

        // assert
        verify(productRepository, times(1)).findById(saved.getId());
        Assertions.assertEquals(productA.id(), productB.id());
    }
    @Test
    @DisplayName("같은 파라미터 2번 조회하면 1번만 DB를 탄다(두번째는 캐시 히트)")
    void cacheHitOnSecondCall_forList() {
        // arrange
        var command = ProductQueryCommand.SearchProducts.of(null, ProductSortType.LATEST, 0, 20
        );
        Page<ProductEntity> stub = new PageImpl<>(
                List.of(ProductEntity.of("상품A", 1000L, 10L, 1L)),
                PageRequest.of(0, 20),
                1
        );
        Mockito.when(productQueryRepository.searchProducts(any())).thenReturn(stub);

        // act
        Page<ProductEntity> first = productService.searchProducts(command);  // miss → DB 1회
        Page<ProductEntity> second = productService.searchProducts(command); // hit  → DB 0회

        // assert
        verify(productQueryRepository, times(1)).searchProducts(any());

        org.assertj.core.api.Assertions.assertThat(first.getContent().get(0).getName())
                .isEqualTo(second.getContent().get(0).getName());
    }

    @Test
    @DisplayName("TTL(30s) 만료 후에는 다시 DB를 탄다")
    void cacheExpiresThenMissAgain_forList() throws Exception {
        // arrange
        var command = ProductQueryCommand.SearchProducts.of(null, ProductSortType.LATEST, 0, 20);
        Page<ProductEntity> stub = new PageImpl<>(
                List.of(ProductEntity.of("상품A", 1000L, 10L, 1L)),
                PageRequest.of(0, 20),
                1
        );
        Mockito.when(productQueryRepository.searchProducts(any())).thenReturn(stub);

        // act
        productService.searchProducts(command); // miss → put
        reset(productQueryRepository);
        Mockito.when(productQueryRepository.searchProducts(any())).thenReturn(stub);

        Thread.sleep(31_000);

        productService.searchProducts(command);

        // assert
        verify(productQueryRepository, times(1)).searchProducts(any());
    }
}
