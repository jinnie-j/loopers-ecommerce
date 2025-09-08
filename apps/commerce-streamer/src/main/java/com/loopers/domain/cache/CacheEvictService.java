package com.loopers.domain.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CacheEvictService {

    private final StringRedisTemplate redis;
    @Value("${app.cache.product-detail}") private String productDetail;
    @Value("${app.cache.product-list}")   private String productList;

    public void onLikeChanged(long productId) {
        evictDetail(productId);
        evictListAll();
    }
    public void onStockAdjusted(long productId) {
        evictDetail(productId);
        evictListAll();
    }

    private void evictDetail(long productId) {
        redis.delete(productDetail + ":" + productId);
    }
    private void evictListAll() {
        var keys = redis.keys(productList + "*");
        if (keys != null && !keys.isEmpty()) redis.delete(keys);
    }
}
