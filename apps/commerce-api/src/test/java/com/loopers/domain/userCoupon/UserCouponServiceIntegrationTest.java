package com.loopers.domain.userCoupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCouponServiceIntegrationTest {

    @Mock
    UserCouponRepository userCouponRepository;

    @InjectMocks
    UserCouponService userCouponService;

    @Test
    @DisplayName("소유한 쿠폰이 ISSUED 상태라면 사용 처리한다")
    void returnUsedStatus_whenIssuedCouponUsed() {
        // arrange
        var command = new UserCouponCommand.Use(1L, 10L);
        var userCoupon = UserCouponEntity.of(1L, 10L); // ISSUED

        when(userCouponRepository.findWithLockByUserIdAndCouponId(1L, 10L))
                .thenReturn(Optional.of(userCoupon));

        // act
        userCouponService.useCoupon(command);

        // assert
        verify(userCouponRepository, times(1))
                .findWithLockByUserIdAndCouponId(1L, 10L);
        assertThat(userCoupon.getStatus()).isEqualTo(UserCouponStatus.USED);
        assertThat(userCoupon.isUsed()).isTrue();
    }

    @Test
    @DisplayName("소유하지 않은 쿠폰이면 NOT_FOUND 에러를 던진다")
    void throwException_whenCouponNotOwned() {
        // arrange
        var command = new UserCouponCommand.Use(1L, 999L);
        when(userCouponRepository.findWithLockByUserIdAndCouponId(1L, 999L))
                .thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> userCouponService.useCoupon(command))
                .isInstanceOf(CoreException.class)
                .extracting("errorType")
                .isEqualTo(ErrorType.NOT_FOUND);

        verify(userCouponRepository).findWithLockByUserIdAndCouponId(1L, 999L);
    }

    @Test
    @DisplayName("이미 사용된 쿠폰이면 BAD_REQUEST 에러를 던진다")
    void throwException_whenAlreadyUsed() {
        // arrange
        var command = new UserCouponCommand.Use(1L, 10L);
        var userCoupon = UserCouponEntity.of(1L, 10L);
        userCoupon.use(); // USED

        when(userCouponRepository.findWithLockByUserIdAndCouponId(1L, 10L))
                .thenReturn(Optional.of(userCoupon));

        // act & assert
        assertThatThrownBy(() -> userCouponService.useCoupon(command))
                .isInstanceOf(CoreException.class)
                .extracting("errorType")
                .isEqualTo(ErrorType.BAD_REQUEST);

        verify(userCouponRepository).findWithLockByUserIdAndCouponId(1L, 10L);
    }
}
