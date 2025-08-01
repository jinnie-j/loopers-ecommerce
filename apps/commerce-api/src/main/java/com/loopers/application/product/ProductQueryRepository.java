package com.loopers.application.product;

import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductSortType;

import java.util.List;

public interface ProductQueryRepository {
    List<ProductEntity> findBySortType(ProductSortType sort);
}
