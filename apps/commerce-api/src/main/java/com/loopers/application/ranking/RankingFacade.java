package com.loopers.application.ranking;

import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.ranking.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankingFacade {

    private final RankingRepository rankingRepository;
    private final ProductRepository productRepository;
    private final BrandService brandService;

    public RankingPage getRankingPage(LocalDate date, int page, int size) {
        String key = RankingPolicy.all(date);
        long total = rankingRepository.size(key);
        if (total == 0) return RankingPage.empty(page, size);

        long start = Math.max(0, (long) (page - 1) * size);
        long end = start + size - 1;

        var rows = rankingRepository.reverseRangeWithScores(key, start, end);
        if (rows.isEmpty()) return RankingPage.empty(page, size);

        List<RankingItem> items = new ArrayList<>(rows.size());
        for (int i = 0; i < rows.size(); i++) {
            var ms = rows.get(i);
            long rank = start + i + 1;
            items.add(new RankingItem(ms.productId(), ms.score(), rank));
        }
        return new RankingPage(items, page, size, total);
    }

    public RankingProductPage getRankingPageWithProducts(LocalDate date, int page, int size) {
        String key = RankingPolicy.all(date);
        long total = rankingRepository.size(key);
        if (total == 0) return RankingProductPage.empty(page, size);

        long start = Math.max(0, (long) (page - 1) * size);
        long end   = start + size - 1;

        var rows = rankingRepository.reverseRangeWithScores(key, start, end);
        if (rows.isEmpty()) return RankingProductPage.empty(page, size);

        // 1) 필요한 상품 id 수집
        List<Long> ids = rows.stream().map(r -> r.productId()).toList();
        Set<Long> idSet = new HashSet<>(ids);

        // 2) 배치 조회
        List<ProductEntity> products = productRepository.findAllById(idSet);
        Map<Long, ProductEntity> byId = products.stream()
                .collect(Collectors.toMap(ProductEntity::getId, p -> p));

        // 3) 브랜드명 조회(간단하게 per-item 호출; 필요하면 배치화)
        List<RankingProductItem> items = new ArrayList<>(rows.size());
        for (int i = 0; i < rows.size(); i++) {
            var ms   = rows.get(i);
            long rank = start + i + 1;

            ProductEntity p = byId.get(ms.productId());
            if (p == null) {
                // 상품이 삭제/비활성화된 경우 스킵하거나 placeholder 처리
                continue;
            }
            String brandName = brandService.getBrandName(p.getBrandId());

            items.add(new RankingProductItem(
                    p.getId(),
                    rank,
                    ms.score(),
                    p.getName(),
                    p.getPrice(),
                    p.getStock(),
                    p.getBrandId(),
                    brandName
            ));
        }
        return new RankingProductPage(items, page, size, total);
    }

    public Optional<RankingItem> getTodayRankOf(long productId) {
        String key = RankingPolicy.all(LocalDate.now());
        var rankOpt = rankingRepository.reverseRank(key, productId);
        if (rankOpt.isEmpty()) return Optional.empty();
        var scoreOpt = rankingRepository.score(key, productId);
        return Optional.of(new RankingItem(productId, scoreOpt.orElse(0.0), rankOpt.get() + 1));
    }
}
