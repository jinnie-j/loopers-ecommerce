package com.loopers.interfaces.api.brand;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BrandV1ApiE2ETest {
    /*
     * 브랜드 E2E 테스트
     * - [x] 존재하는 브랜드 ID로 요청 시, 브랜드 정보를 응답한다.
     * - [x] 존재하지 않는 브랜드 ID로 요청 시, 404 Not Found 응답을 반환한다.
     */

    private final TestRestTemplate testRestTemplate;
    private final BrandRepository brandRepository;
    private final DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Autowired
    public BrandV1ApiE2ETest(TestRestTemplate testRestTemplate,BrandRepository brandRepository, DatabaseCleanUp databaseCleanUp) {
       this.testRestTemplate = testRestTemplate;
        this.brandRepository = brandRepository;
        this.databaseCleanUp = databaseCleanUp;
    }
    private static final String ENDPOINT = "/api/v1/brands";

    @DisplayName("GET /api/v1/brands/{brandId}")
    @Nested
    class getBrand {

        @DisplayName("존재하는 브랜드 ID로 요청 시, 브랜드 정보를 응답한다.")
        @Test
        void returnsBrand_whenBrandIdExist(){
            //arrange
            BrandEntity brandEntity = new BrandEntity("나이키", "스포츠 브랜드");
            brandRepository.save(brandEntity);
            String requestUrl = ENDPOINT + "/" + brandEntity.getId();

            //act
            ParameterizedTypeReference<ApiResponse<BrandResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<BrandResponse>> response =
                    testRestTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<>(null), responseType);
            //assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().name()).isEqualTo(brandEntity.getName()),
                    () -> assertThat(response.getBody().data().description()).isEqualTo(brandEntity.getDescription())
            );
        }

        @DisplayName("존재하지 않는 브랜드 ID로 요청 시, 404 Not Found 응답을 반환한다.")
        @Test
        void returnsNotFound_whenBrandIdDoesNotExist(){
            //arrange
            Long invalidBrandId = 9999L;
            String requestUrl = ENDPOINT + "/" + invalidBrandId;

            //act
            ParameterizedTypeReference<ApiResponse<BrandResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<BrandResponse>> response =
                    testRestTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<>(null), responseType);
            //assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}
