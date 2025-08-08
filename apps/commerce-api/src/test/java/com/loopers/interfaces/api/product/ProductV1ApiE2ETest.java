package com.loopers.interfaces.api.product;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.like.LikeEntity;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Product API E2E 테스트")
public class ProductV1ApiE2ETest {
    /*
     * 상품  조회 E2E 테스트
     * - [x] 상품 목록을 최신순으로 정렬 시, 최근에 등록한 순서로 반환한다.
     * - [x] 상품 목록을 좋아요 순으로 정렬 시, 좋아요가 많은 순서로 반환한다.
     * - [x] 상품 목록을 가격이 낮은 순으로 정렬 시, 가격이 낮은 순서로 반환한다.
     */
    private final TestRestTemplate testRestTemplate;
    private final DatabaseCleanUp databaseCleanUp;
    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final LikeRepository likeRepository;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Autowired
    public ProductV1ApiE2ETest(TestRestTemplate testRestTemplate, ProductRepository productRepository, BrandRepository brandRepository, LikeRepository likeRepository, DatabaseCleanUp databaseCleanUp) {
        this.testRestTemplate = testRestTemplate;
        this.productRepository = productRepository;
        this.brandRepository = brandRepository;
        this.likeRepository = likeRepository;
        this.databaseCleanUp = databaseCleanUp;
    }
    private static final String ENDPOINT = "/api/v1/products";

    private final Long userId = 1L;

    private BrandEntity saveBrand() {
        return brandRepository.save(BrandEntity.of("브랜드", "설명"));
    }

    private ProductEntity saveProduct(String name, Long price) {
        ProductEntity product = ProductEntity.of(name, price, 100L, saveBrand().getId());
        productRepository.save(product);
        return product;
    }

    private void like(ProductEntity product, int count) {
        for (int i = 0; i < count; i++) {
            likeRepository.save(LikeEntity.of((long) i + 100, product.getId()));
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", userId.toString());
        return headers;
    }

    @Test
    @DisplayName("상품의 등록일이 가장 최근인 순서로 상품 목록 조회시, 상품 목록 리스트를 가장 최근에 등록한 상품 순서로 반환한다.")
    void getProducts_sortedByLatest_success() throws InterruptedException {
        ProductEntity oldProduct = productRepository.save(ProductEntity.of("상품1", 1000L, 10L, saveBrand().getId()));
        Thread.sleep(10);
        ProductEntity newProduct = productRepository.save(ProductEntity.of("상품2", 2000L, 10L, saveBrand().getId()));

        String url = ENDPOINT + "?sort=LATEST";
        ResponseEntity<ApiResponse<List<ProductResponse>>> response = testRestTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(null, createHeaders()), new ParameterizedTypeReference<>() {}
        );

        List<ProductResponse> result = response.getBody().data();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(newProduct.getId());
        assertThat(result.get(1).id()).isEqualTo(oldProduct.getId());
    }

    @Test
    @DisplayName("가격 오름차순 정렬 시 가장 저렴한 상품이 먼저 조회된다")
    void getProducts_sortedByPriceAsc() {
        saveProduct("Expensive", 5000L);
        saveProduct("Cheap", 1000L);

        ResponseEntity<ApiResponse<List<ProductResponse>>> response = testRestTemplate.exchange(
                ENDPOINT + "?sort=PRICE_ASC", HttpMethod.GET, new HttpEntity<>(createHeaders()), new ParameterizedTypeReference<>() {}
        );

        List<ProductResponse> result = response.getBody().data();

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).name()).isEqualTo("Cheap");
        assertThat(result.get(1).name()).isEqualTo("Expensive");
    }

    @Test
    @DisplayName("좋아요 수 내림차순 정렬 시 가장 좋아요 많은 상품이 먼저 조회된다")
    void getProducts_sortedByLikesDesc() {
        ProductEntity fewLikes = saveProduct("FewLikes", 1000L);
        ProductEntity manyLikes = saveProduct("ManyLikes", 1000L);

        like(fewLikes, 1);
        like(manyLikes, 3);

        ResponseEntity<ApiResponse<List<ProductResponse>>> response = testRestTemplate.exchange(
                ENDPOINT + "?sort=LIKES_DESC", HttpMethod.GET, new HttpEntity<>(createHeaders()), new ParameterizedTypeReference<>() {}
        );

        List<ProductResponse> result = response.getBody().data();

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).name()).isEqualTo("ManyLikes");
        assertThat(result.get(1).name()).isEqualTo("FewLikes");
    }
}
