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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
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
    private PointRepository pointRepository;
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
            PointEntity pointEntity = pointJpaRepository.save(new PointEntity(userEntity.getId(), 1000L));

            // act
            PointEntity point = pointService.getPoint(userEntity.getId())
                    .orElseThrow(() -> new AssertionError("포인트가 존재하지 않습니다."));

            // assert
            assertAll(
                    () -> assertThat(point.getBalance()).isEqualTo(pointEntity.getBalance()),
                    () -> assertThat(point.getUserId()).isEqualTo(pointEntity.getUserId())
            );
        }

        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.")
        @Test
        void returnsNull_whenInvalidIdIsProvided() {
            // arrange
            long invalidId = 99999L;

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
            long invalidId = 99999L;

            // assert
            assertThatThrownBy(() -> pointService.chargePoint(invalidId, 1000))
                    .isInstanceOf(CoreException.class);
        }
    }

    @Test
    @DisplayName("여러 스레드가 동시에 포인트 차감 시, 포인트가 중복 차감되지 않는다.")
    void concurrentPointDeduction_optimisticLock() throws InterruptedException {
        long userId = 1L;

        int initialBalance = 10000;
        int deductionAmount = 1000;

        PointEntity point = new PointEntity(userId, initialBalance);
        pointRepository.save(point);
        int threadCount = 10;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    pointService.usePoints(userId, (long) deductionAmount);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        PointEntity updated = pointRepository.findByUserId(userId).orElseThrow();
        int expectedBalance = initialBalance - (deductionAmount * successCount.get());

        assertThat(updated.getBalance()).isEqualTo(expectedBalance);
        assertThat(successCount.get()).isLessThanOrEqualTo(threadCount);
        assertThat(failCount.get()).isGreaterThan(0);
    }
}
