package com.loopers.domain.product;

public record ProductInfo (
    Long id,
    String name,
    Long price,
    Long stock,
    Long brandId
) {
        public static ProductInfo from(ProductEntity entity) {
            return new ProductInfo(
                    entity.getId(),
                    entity.getName(),
                    entity.getPrice(),
                    entity.getStock(),
                    entity.getBrandId()
            );
        }
    }

