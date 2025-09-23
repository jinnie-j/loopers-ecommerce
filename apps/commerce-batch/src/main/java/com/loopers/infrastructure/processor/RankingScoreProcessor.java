package com.loopers.infrastructure.processor;

import com.loopers.domain.ranking.PeriodKeyCalculator;
import com.loopers.domain.ranking.ProductDailyMetric;
import com.loopers.domain.ranking.RankingScorePolicy;
import com.loopers.domain.ranking.PeriodAccumulatedScore;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class RankingScoreProcessor {

    private final RankingScorePolicy policy;

    @Bean
    @StepScope
    public ItemProcessor<ProductDailyMetric, PeriodAccumulatedScore> pmProcessor() {
        return row -> {
            double score = policy.score(row.views(), row.likes(), 0, row.orderQty());
            LocalDate w = PeriodKeyCalculator.weekStart(row.metricDate());
            LocalDate m = PeriodKeyCalculator.monthStart(row.metricDate());
            return new PeriodAccumulatedScore(row.productId(), score, w.toString(), m.toString());
        };
    }
}
