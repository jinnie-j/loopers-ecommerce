package com.loopers.domain.userCoupon;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UserCouponTest {

    @Test
    @DisplayName("발급 시 상태는 ISSUED 여야 한다")
    void returnIssuedStatus_whenCreated() {
        // arrange & act
        var userCoupon = UserCouponEntity.of(1L, 10L);

        // assert
        assertThat(userCoupon.getUserId()).isEqualTo(1L);
        assertThat(userCoupon.getCouponId()).isEqualTo(10L);
        assertThat(userCoupon.getStatus()).isEqualTo(UserCouponStatus.ISSUED);
        assertThat(userCoupon.isUsed()).isFalse();
    }

    @Test
    @DisplayName("사용 전 상태(ISSUED)에서만 사용이 가능하며, 호출 후 USED 상태가 된다")
    void returnUsedStatus_whenUseCalledFromIssued() {
        // arrange
        var userCoupon = UserCouponEntity.of(1L, 10L);

        // act
        userCoupon.use();

        // assert
        assertThat(userCoupon.getStatus()).isEqualTo(UserCouponStatus.USED);
        assertThat(userCoupon.isUsed()).isTrue();
    }

    @Test
    @DisplayName("이미 USED 상태에서 다시 use()를 호출하면 예외가 발생한다")
    void throwException_whenUseCalledFromUsed() {
        // arrange
        var userCoupon = UserCouponEntity.of(1L, 10L);
        userCoupon.use();

        // act & assert
        assertThatThrownBy(userCoupon::use)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("사용할 수 없는 쿠폰 상태입니다.");
        assertThat(userCoupon.getStatus()).isEqualTo(UserCouponStatus.USED); // 상태 유지
    }
}
