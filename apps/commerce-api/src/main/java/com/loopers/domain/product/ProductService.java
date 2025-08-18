package com.loopers.domain.product;

import com.loopers.application.product.ProductQueryRepository;
import com.loopers.config.redis.RedisConfig;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@CacheConfig(cacheManager = RedisConfig.CACHE_MANAGER_MASTER)
@RequiredArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductQueryRepository productQueryRepository;

    @Caching(evict = {
            @CacheEvict(cacheNames = RedisConfig.PRODUCT_LIST, allEntries = true)
    })
    public ProductInfo create(ProductCommand.Create productCommand) {
        ProductEntity productEntity = ProductEntity.of(
                productCommand.name(),
                productCommand.price(),
                productCommand.stock(),
                productCommand.brandId()
        );
        return ProductInfo.from(productRepository.save(productEntity));
    }

    @Cacheable(cacheNames = RedisConfig.PRODUCT_DETAIL, key = "#productId", sync = true)
    public ProductInfo getProduct(long productId){
    ProductEntity productEntity = productRepository.findById(productId)
                .orElseThrow(()-> new CoreException(ErrorType.NOT_FOUND));
        return ProductInfo.from(productEntity);
    }

    public List<ProductInfo> getProducts(ProductSortType sort) {
        Pageable pageable = PageRequest.of(0, 100);
        List<ProductEntity> products = productQueryRepository.findBySortType(sort, pageable);
        return products.stream()
                .map(ProductInfo::from)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = RedisConfig.PRODUCT_LIST,
            cacheManager = RedisConfig.CACHE_MANAGER_MASTER,
            keyGenerator = "productListKeyGen",
            sync = true
    )
    public Page<ProductEntity> searchProducts(ProductQueryCommand.SearchProducts command) {
        return productQueryRepository.searchProducts(command);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = RedisConfig.PRODUCT_DETAIL, key = "#productId"),
            @CacheEvict(cacheNames = RedisConfig.PRODUCT_LIST,   allEntries = true)
    })
    @Transactional
    public void decreaseStock(Long productId, Long quantity) {
        ProductEntity product = productRepository.findWithLockById(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다."));

        product.decreaseStock(quantity);
        productRepository.save(product);
    }
}
