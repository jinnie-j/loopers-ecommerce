package com.loopers.interfaces.api.product;

import com.loopers.domain.product.*;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductV1Controller implements ProductV1ApiSpec{

    private final ProductService productService;

    @Override
    public ApiResponse<ProductResponse> create(ProductRequest.Create productRequest) {
        ProductInfo productInfo = productService.create(productRequest.toCommand());
        return ApiResponse.success(ProductResponse.from(productInfo));
    }

    @Override
    public ApiResponse<ProductResponse> getProduct(Long productId) {
        ProductInfo productInfo = productService.getProduct(productId);
        return ApiResponse.success(ProductResponse.from(productInfo));
    }

    @Override
    public ApiResponse<List<ProductResponse>> getProducts(ProductSortType sort) {
        List<ProductInfo> products = productService.getProducts(sort);
        List<ProductResponse> responses = products.stream()
                .map(ProductResponse::from)
                .toList();

        return ApiResponse.success(responses);
    }

    @Override
    public ApiResponse<Page<ProductResponse>> searchProducts(
            @RequestParam(required = false) Long brandId,
            @RequestParam(defaultValue = "LATEST") ProductSortType sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var command = ProductQueryCommand.SearchProducts.of(brandId, sort, page, size);
        Page<ProductEntity> result = productService.searchProducts(command);

        return ApiResponse.success(result.map(ProductResponse::from));
    }
}
