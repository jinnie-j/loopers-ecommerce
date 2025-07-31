package com.loopers.domain.product;

import java.util.Optional;

public interface ProductRepository {
    ProductEntity save(ProductEntity productEntity);

    Optional<ProductEntity> findById(long productId);
}
