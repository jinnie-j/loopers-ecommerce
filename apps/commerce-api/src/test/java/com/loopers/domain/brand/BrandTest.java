package com.loopers.domain.brand;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class BrandTest {

    @DisplayName("브랜드를 생성할때, ")
    @Nested
    class Create {

        @DisplayName("브랜드의 이름이 비어있으면 실패한다.")
        @Test
        void fail_whenBrandNameIsEmpty() {

            assertThrows(IllegalArgumentException.class, () -> BrandEntity.of("", "desc"));
        }

        @DisplayName("브랜드의 이름이 null이면 실패한다.")
        @Test
        void fail_whenBrandNameIsNull() {

            assertThrows(IllegalArgumentException.class, () -> BrandEntity.of(null, "desc"));
        }
    }
}
