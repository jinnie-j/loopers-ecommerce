package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceIntegrationTest {

    @Mock
    CouponRepository couponRepository;

    @InjectMocks
    CouponService couponService;

    private static LocalDateTime future() { return LocalDateTime.now().plusDays(1); }
    private static LocalDateTime past()   { return LocalDateTime.now().minusDays(1); }

    private static CouponEntity fixedAmount(long amount) {
        return CouponEntity.of("A쿠폰", DiscountType.FIXED_AMOUNT, amount, null, future());
    }
    private static CouponEntity fixedRate(double rate) {
        return CouponEntity.of("B쿠폰", DiscountType.FIXED_RATE, null, rate, future());
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("유효한 커맨드면 저장하고 CouponInfo를 반환한다")
        void returnCouponInfo_whenCouponCreated() {
            // arrange
            var coupon = new CouponCommand.Create("신규쿠폰", DiscountType.FIXED_AMOUNT, 2000L, null, future());

            when(couponRepository.save(any(CouponEntity.class)))
                    .then(inv -> inv.getArgument(0, CouponEntity.class));

            // act
            var couponInfo = couponService.create(coupon);

            // assert
            assertThat(couponInfo).isNotNull();

            ArgumentCaptor<CouponEntity> captor = ArgumentCaptor.forClass(CouponEntity.class);
            verify(couponRepository, times(1)).save(captor.capture());
            var saved = captor.getValue();
            assertThat(saved.getName()).isEqualTo("신규쿠폰");
            assertThat(saved.getDisCountType()).isEqualTo(DiscountType.FIXED_AMOUNT);
            assertThat(saved.getDiscountAmount()).isEqualTo(2000L);
            assertThat(saved.getCouponStatus()).isEqualTo(CouponStatus.AVAILABLE);
        }
    }

    @Nested
    @DisplayName("쿠폰 조회")
    class GetAvailableCoupon {

        @Test
        @DisplayName("존재하고 사용 가능하면 엔티티를 반환한다")
        void returnCouponEntity_whenCouponIsAvailable() {
            // arrange
            var coupon = fixedAmount(1000L);
            when(couponRepository.findWithLockById(1L)).thenReturn(Optional.of(coupon));

            // act
            var result = couponService.getAvailableCoupon(1L);

            // assert
            assertThat(result).isSameAs(coupon);
            verify(couponRepository).findWithLockById(1L);
        }

        @Test
        @DisplayName("존재하지 않으면 NOT_FOUND 예외를 던진다")
        void throwException_whenCouponNotFound() {
            // arrange
            when(couponRepository.findWithLockById(99L)).thenReturn(Optional.empty());

            // act & assert
            assertThatThrownBy(() -> couponService.getAvailableCoupon(99L))
                    .isInstanceOf(CoreException.class)
                    .hasMessageContaining("존재하지 않는 쿠폰")
                    .extracting("errorType")
                    .isEqualTo(ErrorType.NOT_FOUND);
        }

        @Test
        @DisplayName("만료되었거나 사용 불가면 BAD_REQUEST 예외를 던진다")
        void throwException_whenCouponUnavailable() {
            // arrange: 만료된 쿠폰
            var expired = CouponEntity.of("만료", DiscountType.FIXED_AMOUNT, 1000L, null, past());
            when(couponRepository.findWithLockById(5L)).thenReturn(Optional.of(expired));

            // act & assert
            assertThatThrownBy(() -> couponService.getAvailableCoupon(5L))
                    .isInstanceOf(CoreException.class)
                    .hasMessageContaining("사용이 불가한 쿠폰")
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("할인 적용")
    class ApplyDiscount {

        @Test
        @DisplayName("정액 쿠폰은 원가에서 할인액을 차감한다(하한 0)")
        void returnDiscountedPrice_whenFixedAmountCoupon() {
            var coupon = fixedAmount(2_000L);

            long discounted1 = couponService.applyDiscount(coupon, 10_000L);
            long discounted2 = couponService.applyDiscount(coupon, 1_000L);

            assertThat(discounted1).isEqualTo(8_000L);
            assertThat(discounted1).isBetween(0L, 10_000L);
            assertThat(discounted2).isZero();
        }

        @Test
        @DisplayName("정률 쿠폰은 소수점 이하는 버림 처리된다")
        void returnDiscountedPrice_whenFixedRateCoupon() {
            var coupon = fixedRate(0.10); // 10%
            long discountedA = couponService.applyDiscount(coupon, 10_000L); // 9000
            long discountedB = couponService.applyDiscount(coupon, 10_005L); // 9004.5 -> 9004

            assertThat(discountedA).isEqualTo((long) (10_000L * (1 - 0.10)));
            assertThat(discountedB).isEqualTo((long) (10_005L * (1 - 0.10)));
            assertThat(discountedA).isBetween(0L, 10_000L);
            assertThat(discountedB).isBetween(0L, 10_005L);
        }
    }
}
