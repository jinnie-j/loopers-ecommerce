package com.loopers.domain.product;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProductRepository {
    ProductEntity save(ProductEntity productEntity);

    Optional<ProductEntity> findById(long productId);

    Optional<ProductEntity> findWithLockById(Long id);

    List<ProductEntity> findAllById(Set<Long> longs);
}
