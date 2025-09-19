package com.loopers.application.job;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class RankingJobTest {

    @Autowired JobLauncher jobLauncher;
    @Autowired @Qualifier("rankingMaterializeJob") Job job;
    @Autowired JdbcTemplate jdbc;

    @Test
    void weekly_and_monthly_snapshots() throws Exception {
        // arrange: 9월 1일~14일 일부 데이터
        insertMetric("2025-09-10", 101, 50, 20, 10);
        insertMetric("2025-09-10", 202, 10,  1,  5);
        insertMetric("2025-09-12", 202, 80,  5, 20);
        insertMetric("2025-09-12", 303,  5, 10,  1);
        insertMetric("2025-09-01", 404, 30,  0,  0);

        JobParameters params = new JobParametersBuilder()
                .addString("fromDate",   "2025-09-01")
                .addString("toDate",     "2025-09-14")
                .addString("targetDate", "2025-09-14")
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        // act
        JobExecution exec = jobLauncher.run(job, params);

        // assert
        assertThat(exec.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        Integer cntStageW = jdbc.queryForObject(
                "SELECT COUNT(*) FROM mv_stage_scores WHERE period_type='W' AND period_key=?",
                Integer.class, java.sql.Date.valueOf("2025-09-08"));
        Integer cntStageM = jdbc.queryForObject(
                "SELECT COUNT(*) FROM mv_stage_scores WHERE period_type='M' AND period_key=?",
                Integer.class, java.sql.Date.valueOf("2025-09-01"));

        assertThat(cntStageW).isGreaterThan(0);
        assertThat(cntStageM).isGreaterThan(0);

        Integer weeklyTop = jdbc.queryForObject(
                "SELECT COUNT(*) FROM mv_product_rank_weekly WHERE period_key=?",
                Integer.class, java.sql.Date.valueOf("2025-09-08"));
        Integer monthlyTop = jdbc.queryForObject(
                "SELECT COUNT(*) FROM mv_product_rank_monthly WHERE period_key=?",
                Integer.class, java.sql.Date.valueOf("2025-09-01"));

        assertThat(weeklyTop).isGreaterThan(0);
        assertThat(monthlyTop).isGreaterThan(0);
    }

    private void insertMetric(String date, long productId, long views, long likes, long qty) {
        jdbc.update(
                "INSERT INTO product_metrics(metric_date, product_id, view_delta, like_delta, sales_qty) " +
                        "VALUES (?, ?, ?, ?, ?)",
                java.sql.Date.valueOf(date), productId, views, likes, qty
        );
    }
}
