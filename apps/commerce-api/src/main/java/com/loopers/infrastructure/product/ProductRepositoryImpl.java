package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;

    @Override
    public ProductEntity save(ProductEntity productEntity) {
        return productJpaRepository.save(productEntity);
    }

    @Override
    public Optional<ProductEntity> findById(long productId) {
        return productJpaRepository.findById(productId);
    }

    @Override
    public Optional<ProductEntity> findWithLockById(Long id) {
        return productJpaRepository.findWithLockById(id);
    }

    @Override
    public List<ProductEntity> findAllById(Set<Long> ids) {return productJpaRepository.findAllById(ids);}
}
