package com.loopers.domain.user;

import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.interfaces.api.user.UserV1Dto;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;


@SpringBootTest
@Transactional
@DisplayName("UserService 통합 테스트")
public class UserServiceIntegrationTest {

    /*
    * 회원가입 통합 테스트
    * - [x] 회원 가입시 User 저장이 수행된다. ( spy 검증 )
    * - [x] 이미 가입된 ID 로 회원가입 시도 시, 실패한다.
    *
    * 내 정보 조회 통합 테스트
    * - [x] 내 정보 조회에 성공할 경우, 해당하는 유저 정보를 응답으로 반환한다.
    * - [x] 존재하지 않는 ID 로 조회할 경우, 404 Not Found 응답을 반환한다.
    */

    @Autowired
    private UserService userService;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;
    @MockitoSpyBean
    private UserJpaRepository userJpaRepository;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("회원 가입 시")
    @Nested
    class SignUp {
        @DisplayName("회원 가입시 User 저장이 수행된다. (spy 검증)")
        @Test
        void saveUser_whenSignUp() {
            // arrange
            UserV1Dto.SignUpRequest signUpRequest = new UserV1Dto.SignUpRequest(
                    "jinnie", "지은", "F", "1997-01-27", "jinnie@naver.com"
            );
            // act
            UserV1Dto.UserResponse userResponse = userService.signUp(signUpRequest);

            //assert
            verify(userJpaRepository).save(any(UserEntity.class));

            assertAll(
                    () -> assertThat(userResponse).isNotNull(),
                    () -> assertThat(userResponse.userId()).isEqualTo(signUpRequest.userId()),
                    () -> assertThat(userResponse.name()).isEqualTo(signUpRequest.name()),
                    () -> assertThat(userResponse.email()).isEqualTo(signUpRequest.email())
            );
        }

        @DisplayName("이미 가입된 ID 로 회원가입 시도 시, 실패한다.")
        @Test
        void throwsException_whenIdIsAlreadyExists() {

            // arrange
            UserV1Dto.SignUpRequest signUpRequest = new UserV1Dto.SignUpRequest(
                    "jinnie", "지은","F", "1997-01-27", "jinnie@naver.com"
            );
            userService.signUp(signUpRequest);
            // act
            CoreException exception = assertThrows(CoreException.class, () -> {
                userService.signUp(signUpRequest);  // 같은 ID로 재가입 시도
            });
            //assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.CONFLICT);
        }
    }

    @DisplayName("회원 정보를 조회할 때, ")
    @Nested
    class Get {
        @DisplayName("해당 ID 의 회원이 존재할 경우, 회원 정보가 반환된다.")
        @Test
        void returnsUserInfo_whenValidIdIsProvided() {
            // arrange
            UserEntity userEntity = userJpaRepository.save(
                    new UserEntity("jinnie","지은","F","1997-01-27", "jinnie@naver.com")
            );
            // act
            UserEntity result = userService.getUserEntity(userEntity.getUserId());

            // assert
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getUserId()).isEqualTo(userEntity.getUserId()),
                    () -> assertThat(result.getName()).isEqualTo(userEntity.getName()),
                    () -> assertThat(result.getEmail()).isEqualTo(userEntity.getEmail())
            );
        }

        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.")
        @Test
        void throwsException_whenInvalidIdIsProvided() {
            // arrange
            String invalidId = "jin";

            // act
            CoreException exception = assertThrows(CoreException.class, () -> {
                userService.getUserEntity(invalidId);
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }
}
