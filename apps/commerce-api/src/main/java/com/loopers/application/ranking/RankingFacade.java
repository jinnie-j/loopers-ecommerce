package com.loopers.application.ranking;

import com.loopers.domain.ranking.RankingItem;
import com.loopers.domain.ranking.RankingPage;
import com.loopers.domain.ranking.RankingPolicy;
import com.loopers.domain.ranking.RankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RankingFacade {

    private final RankingRepository rankingRepository;

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

    public Optional<RankingItem> getTodayRankOf(long productId) {
        String key = RankingPolicy.all(LocalDate.now(RankingPolicy.ZONE));
        var rankOpt = rankingRepository.reverseRank(key, productId);
        if (rankOpt.isEmpty()) return Optional.empty();
        var scoreOpt = rankingRepository.score(key, productId);
        return Optional.of(new RankingItem(productId, scoreOpt.orElse(0.0), rankOpt.get() + 1));
    }
}
