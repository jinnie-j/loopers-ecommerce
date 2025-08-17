package com.loopers.application.product;

import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductQueryCommand;
import com.loopers.domain.product.ProductSortType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductQueryRepository {
    List<ProductEntity> findBySortType(ProductSortType sort, Pageable pageable);

    Page<ProductEntity> searchProducts(ProductQueryCommand.SearchProducts command);
}
