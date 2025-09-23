package com.loopers.application.ranking;

import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.ranking.*;
import com.loopers.infrastructure.ranking.MvRankingQueryRepository;
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
    private final MvRankingQueryRepository mvRankingQueryRepository;

    /** 일(DAY) 단순 랭킹 (상품정보 없음) */
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
    /** 일(DAY) 랭킹 + 상품/브랜드 조인 (Redis) */
    public RankingProductPage getRankingPageWithProducts(LocalDate date, int page, int size) {
        return fromRedisDay(date, page, size);
    }

    /** 기간(DAY/WEEK/MONTH)별 랭킹 + 상품/브랜드 조인 */
    public RankingProductPage getRankingPageWithProducts(PeriodType period, LocalDate date, int page, int size) {
        return switch (period) {
            case DAY   -> fromRedisDay(date, page, size);
            case WEEK  -> fromWeeklyMv(date, page, size);
            case MONTH -> fromMonthlyMv(date, page, size);
        };
    }

    /** 오늘의 단건 랭킹 (옵션) */
    public Optional<RankingItem> getTodayRankOf(long productId) {
        String key = RankingPolicy.all(LocalDate.now());
        var rankOpt = rankingRepository.reverseRank(key, productId);
        if (rankOpt.isEmpty()) return Optional.empty();
        var scoreOpt = rankingRepository.score(key, productId);
        return Optional.of(new RankingItem(productId, scoreOpt.orElse(0.0), rankOpt.get() + 1));
    }

    private RankingProductPage fromRedisDay(LocalDate date, int page, int size) {
        String key = RankingPolicy.all(date);
        long total = rankingRepository.size(key);
        if (total == 0) return RankingProductPage.empty(page, size);

        long start = Math.max(0, (long) (page - 1) * size);
        long end   = start + size - 1;

        var rows = rankingRepository.reverseRangeWithScores(key, start, end);
        if (rows.isEmpty()) return RankingProductPage.empty(page, size);

        // 상품 배치 조회
        List<Long> ids = rows.stream().map(r -> r.productId()).toList();
        Map<Long, ProductEntity> byId = productRepository.findAllById(new HashSet<>(ids))
                .stream().collect(Collectors.toMap(ProductEntity::getId, p -> p));

        List<RankingProductItem> items = new ArrayList<>(rows.size());
        for (int i = 0; i < rows.size(); i++) {
            var ms   = rows.get(i);
            long rank = start + i + 1;

            ProductEntity p = byId.get(ms.productId());
            if (p == null) continue;

            String brandName = brandService.getBrandName(p.getBrandId());

            items.add(new RankingProductItem(
                    p.getId(), rank, ms.score(),
                    p.getName(), p.getPrice(), p.getStock(), p.getBrandId(), brandName
            ));
        }
        return new RankingProductPage(items, page, size, total);
    }

    private RankingProductPage fromWeeklyMv(LocalDate date, int page, int size) {
        int offset = Math.max(0, (page - 1) * size);
        long total = mvRankingQueryRepository.weeklyTotal(date);
        if (total == 0) return RankingProductPage.empty(page, size);

        var rows = mvRankingQueryRepository.weekly(date, offset, size);
        return joinProductsFromMv(rows, page, size, total);
    }

    private RankingProductPage fromMonthlyMv(LocalDate date, int page, int size) {
        int offset = Math.max(0, (page - 1) * size);
        long total = mvRankingQueryRepository.monthlyTotal(date);
        if (total == 0) return RankingProductPage.empty(page, size);

        var rows = mvRankingQueryRepository.monthly(date, offset, size);
        return joinProductsFromMv(rows, page, size, total);
    }

    private RankingProductPage joinProductsFromMv(List<MvRankingQueryRepository.Item> rows,
                                                  int page, int size, long total) {
        List<Long> ids = rows.stream().map(MvRankingQueryRepository.Item::productId).toList();
        Map<Long, ProductEntity> byId = productRepository.findAllById(new HashSet<>(ids))
                .stream().collect(Collectors.toMap(ProductEntity::getId, p -> p));

        List<RankingProductItem> items = new ArrayList<>(rows.size());
        for (var r : rows) {
            ProductEntity p = byId.get(r.productId());
            if (p == null) continue;

            String brandName = brandService.getBrandName(p.getBrandId());
            items.add(new RankingProductItem(
                    p.getId(),
                    (long) r.rankNo(),
                    r.score(),
                    p.getName(), p.getPrice(), p.getStock(), p.getBrandId(), brandName
            ));
        }
        return new RankingProductPage(items, page, size, total);
    }
}
