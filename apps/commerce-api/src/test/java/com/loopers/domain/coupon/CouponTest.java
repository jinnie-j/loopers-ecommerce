package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class CouponTest {

    private static LocalDateTime future() { return LocalDateTime.now().plusDays(1); }
    private static LocalDateTime past()   { return LocalDateTime.now().minusDays(1); }

    @Test
    @DisplayName("정액 쿠폰 생성 성공")
    void returnFixedAmountCoupon_whenCreated() {
        var coupon = CouponEntity.of("신규쿠폰", DiscountType.FIXED_AMOUNT, 2_000L, null, future());

        assertThat(coupon.getName()).isEqualTo("신규쿠폰");
        assertThat(coupon.getDisCountType()).isEqualTo(DiscountType.FIXED_AMOUNT);
        assertThat(coupon.getDiscountAmount()).isEqualTo(2_000L);
        assertThat(coupon.getCouponStatus()).isEqualTo(CouponStatus.AVAILABLE);
        assertThat(coupon.getExpiredAt()).isAfter(LocalDateTime.now());
        assertThat(coupon.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("쿠폰 이름이 비면 생성 실패")
    void throwException_whenNameBlank() {
        assertThatThrownBy(() -> CouponEntity.of("  ", DiscountType.FIXED_AMOUNT, 1000L, null, future()))
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("쿠폰 이름은 필수입니다.");
    }

    @Test
    @DisplayName("할인정책이 null이면 생성 실패")
    void throwException_whenDiscountTypeNull() {
        assertThatThrownBy(() -> CouponEntity.of("A쿠폰", null, 1000L, null, future()))
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("할인 정책은 필수입니다.");
    }

    @Test
    @DisplayName("사용 가능: 상태 AVAILABLE 이고 만료일이 미래")
    void returnTrue_whenAvailableAndFuture() {
        var coupon = CouponEntity.of("A쿠폰", DiscountType.FIXED_AMOUNT, 1000L, null, future());

        assertThat(coupon.getCouponStatus()).isEqualTo(CouponStatus.AVAILABLE);
        assertThat(coupon.getExpiredAt()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("사용 불가: 만료됨")
    void returnFalse_whenExpired() {
        var coupon = CouponEntity.of("A쿠폰", DiscountType.FIXED_AMOUNT, 1000L, null, past());

        assertThat(coupon.getExpiredAt()).isBefore(LocalDateTime.now());
        assertThat(coupon.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("정액 할인: 가격보다 큰 할인은 0원으로 설정")
    void returnZero_whenFixedAmountExceedsPrice() {
        var coupon = CouponEntity.of("A쿠폰", DiscountType.FIXED_AMOUNT, 2_000L, null, future());
        assertThat(coupon.applyDiscount(1_000L)).isZero();

        long discountedNormal = coupon.applyDiscount(10_000L);
        assertThat(discountedNormal)
                .isEqualTo(8_000L);
    }

    @Test
    @DisplayName("정률 할인")
    void returnDiscountedPrice_whenFixedRate() {
        var coupon = CouponEntity.of("B쿠폰", DiscountType.FIXED_RATE, null, 0.10, future()); // 10%
        assertThat(coupon.applyDiscount(10_000L)).isEqualTo((long) (10_000L * (1 - 0.10))); // 9000

        long discountedFraction = coupon.applyDiscount(10_005L); // 9004.5 → 9004
        assertThat(discountedFraction).isEqualTo((long) (10_005L * (1 - 0.10)));
    }

    @Test
    @DisplayName("이미 사용된 쿠폰은 다시 사용할 수 없음")
    void throwException_whenMarkAsUsedTwice() {
        var coupon = CouponEntity.of("A쿠폰", DiscountType.FIXED_AMOUNT, 1000L, null, future());
        // 1회 사용
        assertThat(coupon.getCouponStatus()).isEqualTo(CouponStatus.AVAILABLE);
        coupon.markAsUsed();
        assertThat(coupon.getCouponStatus()).isEqualTo(CouponStatus.USED);
        assertThat(coupon.isAvailable()).isFalse();

        // 2회 사용 시 예외
        assertThatThrownBy(coupon::markAsUsed)
                .isInstanceOf(CoreException.class)
                .hasMessageContaining("이미 사용된 쿠폰");
    }
}
