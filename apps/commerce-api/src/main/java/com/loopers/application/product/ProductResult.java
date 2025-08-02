package com.loopers.application.product;

import com.loopers.domain.product.ProductInfo;

public record ProductResult(
        Long id,
        String name,
        Long price,
        Long stock,
        Long brandId,
        String brandName,
        Long likeCount
) {
    public static ProductResult of(ProductInfo info, String brandName, Long likeCount) {
        return new ProductResult(
                info.id(),
                info.name(),
                info.price(),
                info.stock(),
                info.brandId(),
                brandName,
                likeCount
        );
    }
}
