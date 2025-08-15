package com.loopers.interfaces.api.product;

import com.loopers.domain.product.ProductInfo;

public record ProductResponse(
        Long id,
        String name,
        Long price,
        Long stock,
        Long brandId
) {
    public static ProductResponse from(ProductInfo info) {
        return new ProductResponse(
                info.id(),
                info.name(),
                info.price(),
                info.stock(),
                info.brandId()
        );
    }
}
