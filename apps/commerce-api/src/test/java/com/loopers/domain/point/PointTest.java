package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PointTest {
    /*
    * 단위 테스트
    - [x]  0 이하의 정수로 포인트를 충전시 실패한다.
     */
    @DisplayName("0 이하의 정수로 포인트를 충전시 실패한다.")
    @Test
    void fail_whenChargeAmountIsZeroOrNegative(){
        //arrange
        PointEntity pointEntity = new PointEntity(1000, 1L);

        //act
        assertThatThrownBy(() -> pointEntity.charge(0))
                .isInstanceOf(CoreException.class)
                .extracting("errorType")
                .isEqualTo(ErrorType.BAD_REQUEST);

        assertThatThrownBy(() -> pointEntity.charge(-100))
                .isInstanceOf(CoreException.class)
                .extracting("errorType")
                .isEqualTo(ErrorType.BAD_REQUEST);
    }

    @DisplayName("0 이하의 정수로 포인트를 차감시 실패한다.")
    @Test
    void fail_whenDecreaseAmountIsZeroOrNegative(){
        //arrange
        PointEntity pointEntity = new PointEntity(1000, 1L);

        //act
        assertThatThrownBy(() -> pointEntity.use(0))
                .isInstanceOf(CoreException.class)
                .extracting("errorType")
                .isEqualTo(ErrorType.BAD_REQUEST);

        assertThatThrownBy(() -> pointEntity.use(-100))
                .isInstanceOf(CoreException.class)
                .extracting("errorType")
                .isEqualTo(ErrorType.BAD_REQUEST);
    }
}

