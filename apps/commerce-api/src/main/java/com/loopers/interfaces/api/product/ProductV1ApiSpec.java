package com.loopers.interfaces.api.product;

import com.loopers.domain.product.ProductSortType;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Product API")
@RequestMapping("/api/v1/products")
public interface ProductV1ApiSpec {

    @Operation(summary = "상품 생성")
    ApiResponse<ProductResponse> create(
            @RequestBody ProductRequest.Create productRequest
    );

    @GetMapping("/{productId}")
    ApiResponse<ProductResponse> getProduct(
            @Schema(name = "상품ID", description = "조회할 상품의 ID") @PathVariable Long productId
    );

    @Operation(summary = "상품 목록 조회")
    @GetMapping
    ApiResponse<List<ProductResponse>> getProducts(
            @Schema(description = "정렬 기준", example = "LATEST | PRICE_ASC | LIKES_DESC")
            @RequestParam(name = "sort", required = false, defaultValue = "LATEST")
            ProductSortType sort
    );
}

