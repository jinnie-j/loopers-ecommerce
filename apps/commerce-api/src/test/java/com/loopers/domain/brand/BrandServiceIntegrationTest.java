package com.loopers.domain.brand;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class BrandServiceIntegrationTest {

    @Autowired
    private DatabaseCleanUp databaseCleanUp;
    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Autowired
    private BrandService brandService;

    @DisplayName("브랜드를 조회할 때,")
    @Nested
    class Get{
        @DisplayName("존재하는 브랜드 Id로 조회 시, 브랜드 정보가 반환된다.")
        @Test
        public void returnsBrandInfo_whenValidBrandIdIsProvided(){
            //arrange
            var createdBrand = brandService.create(new BrandCommand.Create("brandName","brandDescription"));

            //act
            var brandInfo = brandService.getBrand(createdBrand.id());

            //assert
            assertEquals(createdBrand.id(), brandInfo.id());
            assertEquals(createdBrand.name(), brandInfo.name());
            assertEquals(createdBrand.description(), brandInfo.description());
        }

        @DisplayName("존재하지 않는 브랜드 Id로 조회 시, NOT FOUND를 반환한다.")
        @Test
        public void fail_whenBrandDoesNotExistOnCharge(){
            //arrange
            Long brandId = 1000L;

            // act
            CoreException exception = assertThrows(CoreException.class, () -> {
                brandService.getBrand(brandId);
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);

        }
    }
}
