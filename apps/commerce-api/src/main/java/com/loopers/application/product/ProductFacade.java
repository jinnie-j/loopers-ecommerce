package com.loopers.application.product;

import com.loopers.application.ranking.RankingFacade;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.ProductSortType;
import com.loopers.domain.product.event.ProductViewed;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductFacade {

    private final ProductService productService;
    private final LikeService likeService;
    private final BrandService brandService;
    private final ProductQueryRepository productQueryRepository;
    private final RankingFacade rankingFacade;
    private final ApplicationEventPublisher publisher;

    /**
     * 단건 조회 - 브랜드, 좋아요 수 포함하여 반환
     */
    public ProductResult getProduct(long productId) {
        ProductInfo productInfo = productService.getProduct(productId);
        String brandName = brandService.getBrandName(productInfo.brandId());
        long likeCount = likeService.countByProductId(productInfo.id());

        // 오늘의 랭킹(없으면 null)
        var rankOpt = rankingFacade.getTodayRankOf(productId);
        Long todayRank = null; Double todayScore = null;
        if (rankOpt.isPresent()) {
            todayRank = rankOpt.get().rank();
            todayScore = rankOpt.get().score();
        }
        publisher.publishEvent(new ProductViewed(productId, /*viewerId*/ null));

        return ProductResult.ofWithRank(productInfo, brandName, likeCount, todayRank, todayScore);
    }

    /**
     * 정렬 조건에 따른 전체 상품 목록 조회
     */
    public List<ProductResult> getProductsSorted(ProductSortType sort, Pageable pageable) {
        List<ProductEntity> products = productQueryRepository.findBySortType(sort, pageable);

        return products.stream()
                .map(product -> {
                    ProductInfo productInfo = ProductInfo.from(product);
                    String brandName = brandService.getBrandName(productInfo.brandId());
                    long likeCount = likeService.countByProductId(productInfo.id());
                    return ProductResult.of(productInfo, brandName, likeCount);
                })
                .toList();
    }
}
