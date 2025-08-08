package com.loopers.application.like;

import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;


@RequiredArgsConstructor
@Component
@Transactional
public class LikeFacade {
    private final LikeService likeService;
    private final ProductService productService;

    public List<ProductInfo> getLikedProducts(long userId){
        return likeService.getLikesByUserId(userId).stream()
                .map(like -> productService.getProduct(like.productId()))
                .toList();
    }
}
