package com.loopers.application.coupon;

import com.loopers.domain.coupon.CouponEntity;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.DiscountType;
import com.loopers.domain.userCoupon.UserCouponCommand;
import com.loopers.domain.userCoupon.UserCouponEntity;
import com.loopers.domain.userCoupon.UserCouponRepository;
import com.loopers.domain.userCoupon.UserCouponService;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CouponFacade 통합 테스트")
@SpringBootTest
public class CouponFacadeIntegrationTest {

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserCouponService userCouponService;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("동일한 쿠폰을 여러 스레드에서 동시에 사용할 경우, 한 번만 성공한다.")
    void concurrentCouponUse_onlyOneSuccess() throws InterruptedException {
        long userId = 1L;

        // 쿠폰 생성
        CouponEntity coupon = couponRepository.save(CouponEntity.of(
                "테스트쿠폰",
                DiscountType.FIXED_AMOUNT,
                1_000L,     // 정액 할인
                null,       // 정률 아님
                LocalDateTime.now().plusMinutes(10)
        ));

        // 발급 (사용 전 상태)
        userCouponRepository.save(UserCouponEntity.of(userId, coupon.getId()));

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    // 쿠폰 사용 처리
                    userCouponService.useCoupon(
                            new UserCouponCommand.Use(userId, coupon.getId())
                    );
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - 1);
    }
}
