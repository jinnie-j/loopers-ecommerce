package com.loopers.application.job;

import com.loopers.domain.ranking.*;
import com.loopers.infrastructure.tasklet.MonthlySnapshotTasklet;
import com.loopers.infrastructure.tasklet.StageCleanupTasklet;
import com.loopers.infrastructure.tasklet.WeeklySnapshotTasklet;
import com.loopers.infrastructure.writer.StageScoreItemWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.*;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class RankingJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager tx;

    @Bean
    public RankingScorePolicy rankingScorePolicy() {
        return new DefaultRankingScorePolicy(0.2, 0.3, 0.5, true);
    }

    @Bean
    public Job rankingMaterializeJob(
            Step stageStep,
            Step rankWeeklyStep,
            Step rankMonthlyStep,
            Step stageCleanupStep
    ) {
        return new JobBuilder("rankingMaterializeJob", jobRepository)
                .start(stageStep)
                .next(rankWeeklyStep)
                .next(rankMonthlyStep)
                .next(stageCleanupStep)
                .build();
    }

    @Bean
    public Step stageStep(
            JdbcPagingItemReader<ProductDailyMetric> pmReader,
            ItemProcessor<ProductDailyMetric, PeriodAccumulatedScore> pmProcessor,
            StageScoreItemWriter stageWriter
    ) {
        return new StepBuilder("stageStep", jobRepository)
                .<ProductDailyMetric, PeriodAccumulatedScore>chunk(2000, tx)
                .reader(pmReader)
                .processor(pmProcessor)
                .writer(stageWriter)
                .build();
    }

    @Bean
    public Step rankWeeklyStep(WeeklySnapshotTasklet t) {
        return new StepBuilder("rankWeeklyStep", jobRepository).tasklet(t, tx).build();
    }

    @Bean
    public Step rankMonthlyStep(MonthlySnapshotTasklet t) {
        return new StepBuilder("rankMonthlyStep", jobRepository).tasklet(t, tx).build();
    }

    @Bean
    public Step stageCleanupStep(StageCleanupTasklet t) {
        return new StepBuilder("stageCleanupStep", jobRepository).tasklet(t, tx).build();
    }
}
