package com.loopers.interfaces.api.point;

import com.loopers.domain.point.PointEntity;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.vo.Birth;
import com.loopers.domain.user.vo.Email;
import com.loopers.infrastructure.point.PointJpaRepository;
import com.loopers.infrastructure.user.UserJpaRepository;
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
import org.springframework.http.*;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PointV1ApiE2ETest {
    /*
     * 포인트 조회 E2E 테스트
     * - [x] 포인트 조회에 성공할 경우, 보유 포인트를 응답으로 반환한다.
     * - [x] X-USER-ID 헤더가 없을 경우, 400 Bad Request 응답을 반환한다.
     *
     * 포인트 충전 E2E 테스트
     * - [] 존재하는 유저가 1000원을 충전할 경우, 충전된 보유 총량을 응답으로 반환한다.
     * - [] 존재하지 않는 유저로 요청할 경우, 404 Not Found 응답을 반환한다.
     */
    private final TestRestTemplate testRestTemplate;
    private final UserJpaRepository userJpaRepository;
    private final PointJpaRepository pointJpaRepository;
    private final DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }
    @Autowired
    public PointV1ApiE2ETest(TestRestTemplate testRestTemplate, UserJpaRepository userJpaRepository, PointJpaRepository pointJpaRepository, DatabaseCleanUp databaseCleanUp) {
        this.testRestTemplate = testRestTemplate;
        this.userJpaRepository = userJpaRepository;
        this.pointJpaRepository = pointJpaRepository;
        this.databaseCleanUp = databaseCleanUp;
    }

    @DisplayName("GET /api/v1/points/{userId}")
    @Nested
    class getPoint {

        private static final String ENDPOINT = "/api/v1/points";

        @DisplayName("포인트 조회에 성공할 경우, 보유 포인트를 응답으로 반환한다.")
        @Test
        void returnsPoint_whenValidUserIdIsProvided() {

            //arrange
            UserEntity userEntity = new UserEntity("jinnie", "지은", Gender.FEMALE, Birth.of("1997-01-27"), Email.of("jinnie@naver.com"));
            userJpaRepository.save(userEntity);

            PointEntity pointEntity = new PointEntity(1000,userEntity.getId());
            pointJpaRepository.save(pointEntity);

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-USER-ID", userEntity.getUserId());
            HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

            //act
            ParameterizedTypeReference<ApiResponse<PointResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.GET, requestEntity, responseType);

            //assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody().data().balance()).isEqualTo(pointEntity.getBalance())
            );

        }

        @DisplayName("X-USER-ID 헤더가 없을 경우, 400 Bad Request 응답을 반환한다.")
        @Test
        void returnsBadRequest_whenXUserIdHeaderIsMissing() {

            //arrange
            HttpEntity<?> requestEntity = new HttpEntity<>(null);

            //act
            ParameterizedTypeReference<ApiResponse<PointResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.GET, requestEntity, responseType);

            //assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        }
    }

    @DisplayName("GET /api/v1/points/{userId}")
    @Nested
    class chargePoint {

        private static final String ENDPOINT_POST = "/api/v1/points/charge";

        @DisplayName("존재하는 유저가 1000원을 충전할 경우, 충전된 보유 총량을 응답으로 반환한다.")
        @Test
        void returnsUpdatedBalance_whenUserCharges1000() {
            //arrange
            UserEntity userEntity = new UserEntity("jinnie", "지은", Gender.FEMALE, Birth.of("1997-01-27"), Email.of("jinnie@naver.com"));
            userJpaRepository.save(userEntity);
            pointJpaRepository.save(new PointEntity(1000,userEntity.getId()));

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-USER-ID", userEntity.getUserId());
            headers.setContentType(MediaType.APPLICATION_JSON);

            PointRequest.PointChargeRequest request = new PointRequest.PointChargeRequest(1000);
            HttpEntity<PointRequest.PointChargeRequest> requestEntity = new HttpEntity<>(request, headers);

            //act
            ParameterizedTypeReference<ApiResponse<PointResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointResponse>> response = testRestTemplate.exchange(ENDPOINT_POST, HttpMethod.POST, requestEntity, responseType);

            //assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().balance()).isEqualTo(2000)
            );
        }

        @DisplayName("존재하지 않는 유저로 요청할 경우, 404 Not Found 응답을 반환한다.")
        @Test
        void returnsNotFound_whenUserDoesNotExist() {
            //arrange
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", "non_existent_user");
            headers.setContentType(MediaType.APPLICATION_JSON);

            PointRequest.PointChargeRequest request = new PointRequest.PointChargeRequest(1000);
            HttpEntity<PointRequest.PointChargeRequest> requestEntity = new HttpEntity<>(request, headers);

            //act
            ParameterizedTypeReference<ApiResponse<PointResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointResponse>> response = testRestTemplate.exchange(ENDPOINT_POST, HttpMethod.POST, requestEntity, responseType);

            //assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL),
                    () -> assertThat(response.getBody().data()).isNull()
            );
        }
    }
    }
