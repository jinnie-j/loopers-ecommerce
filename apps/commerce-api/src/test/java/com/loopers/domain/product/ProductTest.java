package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ProductTest {

    @DisplayName("상품을 생성할때,")
    @Nested
    class Create {

        @Test
        @DisplayName("상품 생성에 성공한다.")
        void createProduct_success() {
            //arrange
            ProductEntity productEntity = ProductEntity.of("shirt", 15000L, 100L, 1L);

            assertThat(productEntity.getStatus()).isEqualTo(ProductStatus.ON_SALE);
        }

        @Test
        @DisplayName("상품 이름이 null이면 BAD REQUEST 예외를 반환한다.")
        void fail_whenNameIsNull() {
            CoreException exception = assertThrows(CoreException.class, () ->
                    ProductEntity.of(null, 15000L, 100L, 1L));

            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @Test
        @DisplayName("상품 가격이 음수이면 BAD REQUEST 예외를 반환한다.")
        void fail_whenPriceIsNegative() {
            CoreException exception = assertThrows(CoreException.class, () ->
                    ProductEntity.of("skirt", -1000L, 100L, 1L));

            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

        @Test
        @DisplayName("상품 재고가 음수이면 BAD REQUEST 예외를 반환한다.")
        void fail_whenStockIsNegative() {
            CoreException exception = assertThrows(CoreException.class, () ->
                    ProductEntity.of("skirt", 15000L, -100L, 1L));

            assertEquals(ErrorType.BAD_REQUEST, exception.getErrorType());
        }

    }
}
