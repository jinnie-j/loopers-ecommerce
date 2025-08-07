package com.loopers.application.coupon;

import com.loopers.application.order.OrderFacade;
import com.loopers.domain.coupon.CouponEntity;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.DiscountType;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.point.PointEntity;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.userCoupon.UserCouponEntity;
import com.loopers.domain.userCoupon.UserCouponRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CouponFacade 통합 테스트")
@SpringBootTest
@Transactional
public class CouponFacadeIntegrationTest {

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private OrderFacade orderFacade;

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

        // 쿠폰 생성 및 발급
        CouponEntity coupon = couponRepository.save(CouponEntity.of(
                "테스트쿠폰",
                DiscountType.FIXED_AMOUNT,
                1000L,             // 할인 금액
                null,              // 정액 할인일 경우 rate는 null
                LocalDateTime.now().plusMinutes(10)
        ));
        coupon.applyDiscount(1_000L);

        userCouponRepository.save(UserCouponEntity.of(userId, coupon.getId()));

        pointRepository.save(new PointEntity(userId, 10_000L));

        OrderCommand.Order orderCommand = new OrderCommand.Order(
                userId,
                List.of(new OrderCommand.OrderItem(1L, 1L, 5_000L)),
                coupon.getId()
        );

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    orderFacade.createOrder(orderCommand);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(threadCount - 1);
    }
}
