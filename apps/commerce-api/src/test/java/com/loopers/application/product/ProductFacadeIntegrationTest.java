package com.loopers.application.product;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.like.LikeEntity;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.product.*;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductFacade 통합 테스트")
@SpringBootTest
class ProductFacadeIntegrationTest {

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("상품 조회 시 브랜드명과 좋아요 수를 포함해 반환한다")
    void getProduct_withBrandAndLikes() {
        // given
        BrandEntity brand = brandRepository.save(BrandEntity.of("Apple", "애플 브랜드"));
        ProductEntity product = productRepository.save(
                new ProductEntity("아이폰", 1200000L, 15L, brand.getId())
        );
        likeRepository.save(LikeEntity.of(1L, product.getId()));
        likeRepository.save(LikeEntity.of(2L, product.getId()));

        // when
        ProductResult result = productFacade.getProduct(product.getId());

        // then
        assertThat(result.name()).isEqualTo("아이폰");
        assertThat(result.brandName()).isEqualTo("Apple");
        assertThat(result.likeCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("정렬 조건이 LATEST일 때 최신순으로 상품 목록이 조회된다")
    void getProductsSorted_latest() {
        // given
        BrandEntity brand = brandRepository.save(BrandEntity.of("Apple", "애플 브랜드"));
        productRepository.save(new ProductEntity("구형 모델", 500000L, 3L, brand.getId()));
        productRepository.save(new ProductEntity("신형 모델", 1500000L, 2L, brand.getId()));

        // when
        List<ProductResult> results = productFacade.getProductsSorted(ProductSortType.LATEST);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).name()).isEqualTo("신형 모델");
        assertThat(results.get(1).name()).isEqualTo("구형 모델");
    }

    @Test
    @DisplayName("정렬 조건이 PRICE_ASC일 때 가격 오름차순으로 상품 목록이 조회된다")
    void getProductsSorted_priceAsc() {
        // given
        BrandEntity brand = brandRepository.save(BrandEntity.of("Apple", "애플 브랜드"));
        productRepository.save(new ProductEntity("비싼 상품", 200000L, 5L, brand.getId()));
        productRepository.save(new ProductEntity("저렴한 상품", 100000L, 3L, brand.getId()));

        // when
        List<ProductResult> results = productFacade.getProductsSorted(ProductSortType.PRICE_ASC);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).name()).isEqualTo("저렴한 상품");
        assertThat(results.get(1).name()).isEqualTo("비싼 상품");
    }

    @Test
    @DisplayName("정렬 조건이 LIKES_DESC일 때 좋아요 수 내림차순으로 상품 목록이 조회된다")
    void getProductsSorted_likesDesc() {
        // given
        BrandEntity brand = brandRepository.save(BrandEntity.of("Apple", "애플 브랜드"));
        ProductEntity productA = productRepository.save(new ProductEntity("상품A", 10000L, 1L, brand.getId()));
        ProductEntity productB = productRepository.save(new ProductEntity("상품B", 10000L, 1L, brand.getId()));

        likeRepository.save(LikeEntity.of(1L, productA.getId()));
        likeRepository.save(LikeEntity.of(2L, productA.getId()));
        likeRepository.save(LikeEntity.of(3L, productB.getId()));

        // when
        List<ProductResult> results = productFacade.getProductsSorted(ProductSortType.LIKES_DESC);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).likeCount()).isGreaterThanOrEqualTo(results.get(1).likeCount());
    }
}
