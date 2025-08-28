package com.loopers.domain.product;

import com.loopers.application.order.OrderCriteria;
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
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

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

    @Transactional(readOnly = true)
    public void validateAvailability(List<OrderCriteria.CreateWithPayment.Item> items) {
        Map<Long, Long> need = items.stream()
                .collect(toMap(OrderCriteria.CreateWithPayment.Item::productId, OrderCriteria.CreateWithPayment.Item::quantity, Long::sum));

        List<ProductEntity> products = productRepository.findAllById(need.keySet());
        Set<Long> found = products.stream().map(ProductEntity::getId).collect(toSet());

        // 존재하지 않음 → 404
        var missing = need.keySet().stream().filter(id -> !found.contains(id)).toList();
        if (!missing.isEmpty()) throw new CoreException(ErrorType.NOT_FOUND, "상품 없음: " + missing);

        // 재고 부족 → 400
        for (ProductEntity product : products) {
            long required = need.get(product.getId());
            if (product.getStock() < required) throw new CoreException(ErrorType.BAD_REQUEST, "재고 부족: " + product.getId());
        }
    }
}
