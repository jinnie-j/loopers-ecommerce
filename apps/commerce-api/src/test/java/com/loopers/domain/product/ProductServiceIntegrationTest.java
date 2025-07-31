package com.loopers.domain.product;

import com.loopers.domain.brand.BrandCommand;
import com.loopers.domain.brand.BrandInfo;
import com.loopers.domain.brand.BrandService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
@DisplayName("ProductService 통합 테스트")
public class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;
    @Autowired
    private BrandService brandService;

    @Nested
    class Create{
        @DisplayName("상품이 생성되면, 상품 정보가 반환된다.")
        @Test
        void returnProduct_whenProductIsCreated(){

            //arrange
            BrandInfo brandInfo = brandService.create(new BrandCommand.Create("brandName", "brandDescription"));
            ProductCommand.Create command = new ProductCommand.Create(
                    "productName",brandInfo.id(),10000L,10L);

            //act
            ProductInfo ProductInfo = productService.create(command);
            var productInfo = productService.getProduct(ProductInfo.id());

            //assert
            assertEquals(ProductInfo.id(), productInfo.id());
            assertEquals("productName", productInfo.name());
            assertEquals(10000L, productInfo.price());
            assertEquals(10L, productInfo.stock());
        }
    }
}
