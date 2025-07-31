package com.loopers.domain.like;


import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;


public class LikeTest {

    @DisplayName("좋아요를 생성할때")
    @Nested
    class Create{

        @Test
        @DisplayName("좋아요 생성에 성공한다.")
        void createLike_success(){
            //arrange
            Long userId = 1L;
            Long productId = 10L;

            //act
            LikeEntity likeEntity = LikeEntity.of(userId, productId);

            //assert
            assertThat(likeEntity).isNotNull();
            assertThat(likeEntity.getProductId()).isEqualTo(productId);
            assertThat(likeEntity.getUserId()).isEqualTo(userId);
        }
    }

    @Test
    @DisplayName("userId가 null인 경우 BAD REQUEST 예외를 반환한다.")
    void fail_whenUserIdIsNull(){

        //arrange
        CoreException exception = assertThrows(CoreException.class, () ->
                LikeEntity.of(null, 10L));

        //act & assert
        assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
    }

    @Test
    @DisplayName("productId가 null인 경우 BAD REQUEST 예외를 반환한다.")
    void fail_whenProductIdIsNull(){

        //arrange
        CoreException exception = assertThrows(CoreException.class, () ->
                LikeEntity.of(10L, null));

        //act & assert
        assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
    }

}
