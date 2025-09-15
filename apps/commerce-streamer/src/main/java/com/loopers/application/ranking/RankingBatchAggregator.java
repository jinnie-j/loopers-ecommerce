package com.loopers.application.ranking;


import com.loopers.domain.ranking.RankingPolicy;
import com.loopers.domain.ranking.RankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RankingBatchAggregator {

    private final RankingRepository rankingRepository;

    public void apply(LocalDate date, Map<String, Double> productDeltas) {
        if (productDeltas == null || productDeltas.isEmpty()) return;
        rankingRepository.incrementScores(RankingPolicy.all(date), productDeltas, RankingPolicy.expireAt());
    }
}
