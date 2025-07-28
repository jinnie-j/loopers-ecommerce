package com.loopers.interfaces.api.user;

import com.loopers.domain.user.Gender;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.vo.Birth;
import com.loopers.domain.user.vo.Email;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.interfaces.api.ApiResponse;
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

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserV1ApiE2ETest {
    /*
     * 회원가입 E2E 테스트
     * - [x] 회원 가입이 성공할 경우, 생성된 유저 정보를 응답으로 반환한다.
     * - [x] 회원 가입 시에 성별이 없을 경우, 400 Bad Request 응답을 반환한다.
     *
     * 내 정보 조회 E2E 테스트
     * - [x] 내 정보 조회에 성공할 경우, 해당하는 유저 정보를 응답으로 반환한다.
     * - [x] 존재하지 않는 ID 로 조회할 경우, 404 Not Found 응답을 반환한다.
     */

    private final TestRestTemplate testRestTemplate;
    private final UserJpaRepository userJpaRepository;

    @Autowired
    public UserV1ApiE2ETest(TestRestTemplate testRestTemplate, UserJpaRepository userJpaRepository) {
        this.testRestTemplate = testRestTemplate;
        this.userJpaRepository = userJpaRepository;
    }

    @DisplayName("POST /api/v1/users")
    @Nested
    class Join {
        private static final String ENDPOINT = "/api/v1/users";

        @DisplayName("회원 가입이 성공할 경우, 생성된 유저 정보를 응답으로 반환한다.")
        @Test
        void returnsUserInfo_whenJoinIsSuccessful() {

            //arrange
            UserV1Dto.SignUpRequest signUpRequest = new UserV1Dto.SignUpRequest(
                    "jinnie",
                    "지은",
                    "F",
                    "1997-01-27",
                    "jinnie@naver.com"
            );

            //act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(signUpRequest), responseType);

            //assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().name()).isEqualTo(signUpRequest.name()),
                    () -> assertThat(response.getBody().data().gender()).isEqualTo(signUpRequest.gender())
            );
        }

        @DisplayName("회원 가입 시에 성별이 없을 경우, 400 Bad Request 응답을 반환한다.")
        @Test
        void returnsBadRequest_whenGenderIsMissing() {

            //arrange
            UserV1Dto.SignUpRequest signUpRequest = new UserV1Dto.SignUpRequest(
                    "jinnie",
                    "지은",
                    null,
                    "1997-01-27",
                    "jinnie@naver.com"

            );
            //act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, new HttpEntity<>(signUpRequest), responseType);

            //assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
                    () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL),
                    () -> assertThat(response.getBody().data()).isNull()
            );
        }
    }

    @DisplayName("GET /api/v1/users/{id}")
    @Nested
    class getUserInfo {

        private static final Function<String,String> ENDPOINT_GET = id -> "/api/v1/users/" + id;

        @DisplayName("내 정보 조회에 성공할 경우, 해당하는 유저 정보를 응답으로 반환한다.")
        @Test
        void returnsUserInfo_whenGetUserInfoIsSuccessful() {

            //arrange
            UserEntity userEntity = new UserEntity("jinnie","지은", Gender.FEMALE, Birth.of("1997-01-27"), Email.of("jinnie@naver.com"));
            userJpaRepository.save(userEntity);

            String requestUrl = ENDPOINT_GET.apply(userEntity.getUserId());

            //act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                    testRestTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<>(null), responseType);

            //assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().name()).isEqualTo(userEntity.getName())
            );
        }

        @DisplayName("존재하지 않는 ID 로 조회할 경우, 404 Not Found 응답을 반환한다.")
        @Test
        void returnsBadRequest_whenGetUserInfoIsNotFound() {
            //arrange
            String userId = "non_existent_user";
            String requestUrl = ENDPOINT_GET.apply(userId);

            //act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                    testRestTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<>(null), responseType);

            //assert
            assertAll(
                    () -> assertTrue(response.getStatusCode().is4xxClientError()),
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND)
            );
        }
    }
}
