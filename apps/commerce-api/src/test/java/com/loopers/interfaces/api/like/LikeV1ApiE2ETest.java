package com.loopers.interfaces.api.like;

import com.loopers.domain.like.LikeEntity;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LikeV1ApiE2ETest  {
    /*
     * 좋아요 등록 E2E 테스트
     * - [x] 좋아요가 등록되어 있지 않은 상태에서 요청 시, 200 OK를 반환한다.
     * - [x] 이미 좋아요가 등록된 상태에서 다시 요청 시, 200 OK를 반환한다.
     *
     * 좋아요 취소 E2E 테스트
     * - [x] 좋아요가 등록된 상태에서 취소 요청 시, 200 OK를 반환한다.
     * - [x] 좋아요가 등록되지 않은 상태에서 취소 요청 시, 200 OK를 반환한다.
     *
     * 좋아요 목록 조회 E2E 테스트
     * - [x] 유저가 좋아요한 상품 목록을 응답으로 반환한다.
     */

    private final TestRestTemplate testRestTemplate;
    private final DatabaseCleanUp databaseCleanUp;
    private final ProductRepository productRepository;
    private final LikeRepository likeRepository;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Autowired
    public LikeV1ApiE2ETest(TestRestTemplate testRestTemplate, ProductRepository productRepository, LikeRepository likeRepository ,DatabaseCleanUp databaseCleanUp) {
        this.testRestTemplate = testRestTemplate;
        this.productRepository = productRepository;
        this.likeRepository = likeRepository;
        this.databaseCleanUp = databaseCleanUp;
    }
    private static final String ENDPOINT = "/api/v1/likes/products";

    private final Long userId = 1L;

    private ProductEntity saveProduct() {
        ProductEntity product = ProductEntity.of("상품명", 1000L, 10L, 1L);
        return productRepository.save(product);
    }
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", userId.toString());
        return headers;
    }

    @Nested
    @DisplayName("좋아요 등록")
    class Like {

        @Test
        @DisplayName("좋아요가 등록되지 않은 상태에서 요청 시, 200 OK를 반환한다.")
        void returnsSuccess_like_whenNotLiked() {
            // arrange
            ProductEntity product = saveProduct();
            String requestUrl = ENDPOINT + "/" + product.getId();

            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ParameterizedTypeReference<ApiResponse<LikeResponse>> responseType = new ParameterizedTypeReference<>() {};

            // act
            ResponseEntity<ApiResponse<LikeResponse>> response =
                    testRestTemplate.exchange(requestUrl, HttpMethod.POST, entity, responseType);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(likeRepository.find(userId, product.getId())).isPresent();
        }

        @Test
        @DisplayName("좋아요가 등록된 상태에서 요청 시, 200 OK를 반환한다.")
        void returnsSuccess_like_whenLiked() {
            // arrange
            ProductEntity product = saveProduct();
            likeRepository.save(LikeEntity.of(userId, product.getId()));
            String requestUrl = ENDPOINT + "/" + product.getId();

            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ParameterizedTypeReference<ApiResponse<LikeResponse>> responseType = new ParameterizedTypeReference<>() {};

            // act
            ResponseEntity<ApiResponse<LikeResponse>> response =
                    testRestTemplate.exchange(requestUrl, HttpMethod.POST, entity, responseType);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(likeRepository.find(userId, product.getId())).isPresent();
        }
    }
    @Nested
    @DisplayName("좋아요 취소")
    class UnlikeTest {

        @Test
        @DisplayName("좋아요가 등록된 상태에서 취소 요청 시, 200 OK를 반환한다.")
        void returnsSuccess_unlike_whenLiked() {
            ProductEntity product = saveProduct();
            likeRepository.save(LikeEntity.of(userId, product.getId()));

            String requestUrl = ENDPOINT + "/" + product.getId();
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ParameterizedTypeReference<ApiResponse<LikeResponse>> responseType = new ParameterizedTypeReference<>() {};

            ResponseEntity<ApiResponse<LikeResponse>> response =
                    testRestTemplate.exchange(requestUrl, HttpMethod.DELETE, entity, responseType);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(likeRepository.find(userId, product.getId())).isNotPresent();
        }

        @Test
        @DisplayName("좋아요가 등록되지 않은 상태에서 취소 요청 시, 200 OK를 반환한다.")
        void returnsSuccess_unlike_whenNotLiked() {
            ProductEntity product = saveProduct();

            String requestUrl = ENDPOINT + "/" + product.getId();
            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ParameterizedTypeReference<ApiResponse<LikeResponse>> responseType = new ParameterizedTypeReference<>() {};

            ResponseEntity<ApiResponse<LikeResponse>> response =
                    testRestTemplate.exchange(requestUrl, HttpMethod.DELETE, entity, responseType);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(likeRepository.find(userId, product.getId())).isNotPresent();
        }
    }

    @Nested
    @DisplayName("좋아요 조회")
    class getLikes {
        @Test
        @DisplayName("유저가 좋아요한 상품 목록을 응답으로 반환한다.")
        void returnsLikedProductsByUserId() {
            ProductEntity p1 = saveProduct();
            ProductEntity p2 = saveProduct();
            likeRepository.save(LikeEntity.of(userId, p1.getId()));
            likeRepository.save(LikeEntity.of(userId, p2.getId()));

            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ParameterizedTypeReference<ApiResponse<List<LikeResponse>>> responseType = new ParameterizedTypeReference<>() {};

            ResponseEntity<ApiResponse<List<LikeResponse>>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.GET, entity, responseType);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().data())
                    .extracting("productId")
                    .containsExactlyInAnyOrder(p1.getId(), p2.getId());
        }
    }
}

