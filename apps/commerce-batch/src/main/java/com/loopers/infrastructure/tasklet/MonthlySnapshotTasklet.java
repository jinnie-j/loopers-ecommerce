package com.loopers.infrastructure.tasklet;

import com.loopers.domain.ranking.PeriodKeyCalculator;
import com.loopers.domain.ranking.ProductScore;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MonthlySnapshotTasklet implements Tasklet {

    private final JdbcTemplate jdbc;

    @Override
    public RepeatStatus execute(StepContribution c, ChunkContext ctx) {
        String target = c.getStepExecution().getJobParameters().getString("targetDate");
        LocalDate t   = LocalDate.parse(target);

        var ms = PeriodKeyCalculator.monthStart(t);
        var me = PeriodKeyCalculator.monthEnd(t);
        String key = ms.toString();

        jdbc.update("DELETE FROM mv_product_rank_monthly WHERE period_key=?", key);

        List<ProductScore> rows = jdbc.query("""
            SELECT product_id, score
            FROM mv_stage_scores
            WHERE period_type='M' AND period_key=?
            ORDER BY score DESC
            LIMIT 100
        """, (rs, i) -> new ProductScore(rs.getLong(1), rs.getDouble(2)), key);

        int rank = 1;
        for (ProductScore r : rows) {
            jdbc.update("""
              INSERT INTO mv_product_rank_monthly(period_start, period_end, period_key, product_id, rank_no, score)
              VALUES(?,?,?,?,?,?)
              ON DUPLICATE KEY UPDATE rank_no=VALUES(rank_no), score=VALUES(score)
            """, ms, me, key, r.productId(), rank++, r.score());
        }
        return RepeatStatus.FINISHED;
    }
}
