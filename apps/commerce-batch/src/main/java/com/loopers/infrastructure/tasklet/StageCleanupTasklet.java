package com.loopers.infrastructure.tasklet;

import com.loopers.domain.ranking.PeriodKeyCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class StageCleanupTasklet implements Tasklet {

    private final JdbcTemplate jdbc;

    @Override
    public RepeatStatus execute(StepContribution c, ChunkContext ctx) {
        String target = c.getStepExecution().getJobParameters().getString("targetDate");
        LocalDate t   = LocalDate.parse(target);

        String wKey = PeriodKeyCalculator.weekStart(t).toString();
        String mKey = PeriodKeyCalculator.monthStart(t).toString();

        jdbc.update("DELETE FROM mv_stage_scores WHERE period_type='W' AND period_key=?", wKey);
        jdbc.update("DELETE FROM mv_stage_scores WHERE period_type='M' AND period_key=?", mKey);

        return RepeatStatus.FINISHED;
    }
}
