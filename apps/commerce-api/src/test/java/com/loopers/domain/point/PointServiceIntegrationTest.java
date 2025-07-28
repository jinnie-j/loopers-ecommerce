package com.loopers.domain.point;

import com.loopers.domain.user.Gender;
import com.loopers.domain.user.UserEntity;

import com.loopers.domain.user.vo.Birth;
import com.loopers.domain.user.vo.Email;
import com.loopers.infrastructure.point.PointJpaRepository;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.support.error.CoreException;
import com.loopers.utils.DatabaseCleanUp;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@Transactional
@DisplayName("PointService 통합 테스트")
public class PointServiceIntegrationTest {
    /*
     * 포인트 조회 통합 테스트
     * - [x]  해당 ID 의 회원이 존재할 경우, 보유 포인트가 반환된다.
     * - [x]  해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.
     *
     * 포인트 충전 통합 테스트
     * - [ ] 존재하지 않는 유저 ID로 충전을 시도한 경우, 실패한다.
     */

    @Autowired
    private PointService pointService;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;
    @Autowired
    private PointJpaRepository pointJpaRepository;
    @Autowired
    private UserJpaRepository userJpaRepository;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Nested
    class Get {
        @DisplayName("해당 ID 의 회원이 존재할 경우, 보유 포인트가 반환된다.")
        @Test
        void returnsPoint_whenValidIdIsProvided() {
            // arrange
            UserEntity userEntity = userJpaRepository.save(
                    new UserEntity("jinnie", "지은", Gender.FEMALE, Birth.of("1997-01-27"), Email.of("jinnie@naver.com"))
            );
            PointEntity pointEntity = pointJpaRepository.save(new PointEntity(1000, userEntity.getUserId()));

            // act
            Optional<PointEntity> point = pointService.getPoint(userEntity.getUserId());

            // assert
            assertAll(
                    () -> assertThat(point).isNotNull(),
                    () -> assertThat(point.get().getBalance()).isEqualTo(pointEntity.getBalance()),
                    () -> assertThat(point.get().getUserId()).isEqualTo(pointEntity.getUserId())
            );
        }

        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.")
        @Test
        void returnsNull_whenInvalidIdIsProvided() {
            // arrange
            String invalidId = "non_existent_user";

            // act
            Optional<PointEntity> point = pointService.getPoint(invalidId);

            // assert
            assertThat(point).isEmpty();
        }
    }

    @Nested
    class Charge {
        @DisplayName("존재하지 않는 유저 ID로 충전을 시도한 경우, 실패한다.")
        @Test
        void fail_whenUserDoesNotExistOnCharge() {
            // arrange
            String invalidId = "non_existent_user";

            // assert
            assertThatThrownBy(() -> pointService.chargePoint(invalidId, 1000))
                    .isInstanceOf(CoreException.class);
        }
    }
}
