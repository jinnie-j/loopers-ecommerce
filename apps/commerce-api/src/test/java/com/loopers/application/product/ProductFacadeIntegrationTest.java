package com.loopers.application.product;

import com.loopers.application.ranking.RankingFacade;
import com.loopers.config.redis.RedisConfig;
import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.like.LikeEntity;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.product.*;
import com.loopers.domain.product.event.ProductViewed;
import com.loopers.domain.ranking.RankingItem;
import com.loopers.infrastructure.kafka.AfterCommitKafkaBridge;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("ProductFacade 통합 테스트")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.autoconfigure.exclude=" +
                        "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration",
                "spring.cache.type=NONE",
                "spring.main.allow-bean-definition-overriding=true"
        }
)
@RecordApplicationEvents
class ProductFacadeIntegrationTest {

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private LikeRepository likeRepository;

    @MockitoBean private RankingFacade rankingFacade;

    @MockitoBean private AfterCommitKafkaBridge bridge;

    @MockitoBean private KafkaTemplate<Object, Object> kafka;

    @MockitoBean private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ApplicationEvents events;

    @TestConfiguration
    static class NoRedisCacheConfig {
        @Bean(name = RedisConfig.CACHE_MANAGER_MASTER)
        public CacheManager masterNoOpCacheManager() {
            return new NoOpCacheManager();
        }
    }

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("상품 조회 시 브랜드명과 좋아요 수를 포함해 반환한다")
    void getProduct_withBrandAndLikes() {
        // arrange
        BrandEntity brand = brandRepository.save(BrandEntity.of("Apple", "애플 브랜드"));
        ProductEntity product = productRepository.save(
                ProductEntity.of("아이폰", 1200000L, 15L, brand.getId())
        );
        likeRepository.save(LikeEntity.of(1L, product.getId()));
        likeRepository.save(LikeEntity.of(2L, product.getId()));

        // act
        ProductResult result = productFacade.getProduct(product.getId());

        // assert
        assertThat(result.name()).isEqualTo("아이폰");
        assertThat(result.brandName()).isEqualTo("Apple");
        assertThat(result.likeCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("정렬 조건이 LATEST일 때 최신순으로 상품 목록이 조회된다")
    void getProductsSorted_latest() {
        // arrange
        BrandEntity brand = brandRepository.save(BrandEntity.of("Apple", "애플 브랜드"));
        productRepository.save(ProductEntity.of("구형 모델", 500000L, 3L, brand.getId()));
        productRepository.save(ProductEntity.of("신형 모델", 1500000L, 2L, brand.getId()));
        Pageable pageable = PageRequest.of(0, 10);

        // act
        List<ProductResult> results = productFacade.getProductsSorted(ProductSortType.LATEST, pageable);

        // assert
        assertThat(results).hasSize(2);
        assertThat(results.get(0).name()).isEqualTo("신형 모델");
        assertThat(results.get(1).name()).isEqualTo("구형 모델");
    }

    @Test
    @DisplayName("정렬 조건이 PRICE_ASC일 때 가격 오름차순으로 상품 목록이 조회된다")
    void getProductsSorted_priceAsc() {
        // arrange
        BrandEntity brand = brandRepository.save(BrandEntity.of("Apple", "애플 브랜드"));
        productRepository.save(ProductEntity.of("비싼 상품", 200000L, 5L, brand.getId()));
        productRepository.save(ProductEntity.of("저렴한 상품", 100000L, 3L, brand.getId()));
        Pageable pageable = PageRequest.of(0, 10);

        // act
        List<ProductResult> results = productFacade.getProductsSorted(ProductSortType.PRICE_ASC, pageable);

        // assert
        assertThat(results).hasSize(2);
        assertThat(results.get(0).name()).isEqualTo("저렴한 상품");
        assertThat(results.get(1).name()).isEqualTo("비싼 상품");
    }

    @Test
    @DisplayName("정렬 조건이 LIKES_DESC일 때 좋아요 수 내림차순으로 상품 목록이 조회된다")
    void getProductsSorted_likesDesc() {
        // arrange
        BrandEntity brand = brandRepository.save(BrandEntity.of("Apple", "애플 브랜드"));
        ProductEntity productA = productRepository.save(ProductEntity.of("상품A", 10000L, 1L, brand.getId()));
        ProductEntity productB = productRepository.save(ProductEntity.of("상품B", 10000L, 1L, brand.getId()));

        likeRepository.save(LikeEntity.of(1L, productA.getId()));
        likeRepository.save(LikeEntity.of(2L, productA.getId()));
        likeRepository.save(LikeEntity.of(3L, productB.getId()));
        Pageable pageable = PageRequest.of(0, 10);

        // act
        List<ProductResult> results = productFacade.getProductsSorted(ProductSortType.LIKES_DESC, pageable);

        // assert
        assertThat(results).hasSize(2);
        assertThat(results.get(0).likeCount()).isGreaterThanOrEqualTo(results.get(1).likeCount());
    }

    @Test
    @DisplayName("랭킹이 있으면 todayRank/todayScore를 세팅하고 조회 이벤트를 발행한다")
    void getProduct_todayRank_todayScore() {
        // arrange
        var brand = brandRepository.save(BrandEntity.of("Apple", "애플"));
        var product = productRepository.save(ProductEntity.of("아이폰", 1_200_000L, 15L, brand.getId()));
        likeRepository.save(LikeEntity.of(1L, product.getId()));
        likeRepository.save(LikeEntity.of(2L, product.getId()));

        when(rankingFacade.getTodayRankOf(product.getId()))
                .thenReturn(Optional.of(new RankingItem(product.getId(), 9.9, 3L)));

        // act
        ProductResult r = productFacade.getProduct(product.getId());

        // assert
        assertThat(r.name()).isEqualTo("아이폰");
        assertThat(r.brandName()).isEqualTo("Apple");
        assertThat(r.likeCount()).isEqualTo(2L);
        assertThat(r.todayRank()).isEqualTo(3L);
        assertThat(r.todayScore()).isEqualTo(9.9);

        // assert – 조회 이벤트 발행 확인
        long viewEvents = events.stream(ProductViewed.class).count();
        assertThat(viewEvents).isEqualTo(1L);
    }

    @Test
    @DisplayName("랭킹이 없으면 todayRank/todayScore는 null이고 조회 이벤트는 발행된다")
    void getProduct_todayRank_todayScore_null() {
        var brand = brandRepository.save(BrandEntity.of("Brand", "설명"));
        var product = productRepository.save(ProductEntity.of("상품", 10_000L, 5L, brand.getId()));

        when(rankingFacade.getTodayRankOf(product.getId())).thenReturn(Optional.empty());

        ProductResult r = productFacade.getProduct(product.getId());

        assertThat(r.todayRank()).isNull();
        assertThat(r.todayScore()).isNull();

        long viewEvents = events.stream(ProductViewed.class).count();
        assertThat(viewEvents).isEqualTo(1L);
    }
}
