package com.loopers.application.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.ZoneId;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class RankingBatchScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final JobLauncher jobLauncher;
    private final Job rankingMaterializeJob;

    // 어제분 누적 + 오늘 기준으로 주/월 스냅샷 갱신
    @Scheduled(cron = "0 5 1 * * *", zone = "Asia/Seoul")
    public void dailyMaterialize() throws Exception {
        LocalDate today = LocalDate.now(KST);
        LocalDate yesterday = today.minusDays(1);

        JobParameters params = new JobParametersBuilder()
                .addString("fromDate",   yesterday.toString())
                .addString("toDate",     yesterday.toString())
                .addString("targetDate", today.toString())
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(rankingMaterializeJob, params);
    }

    // 주간 마감: 매주 월 — 지난주 확정 스냅샷
    @Scheduled(cron = "0 10 0 * * MON", zone = "Asia/Seoul")
    public void weeklyClose() throws Exception {
        LocalDate monday = LocalDate.now(KST);
        LocalDate sunday = monday.minusDays(1);
        LocalDate lastMonday = sunday.minusDays(6);

        JobParameters params = new JobParametersBuilder()
                .addString("fromDate",   lastMonday.toString())
                .addString("toDate",     sunday.toString())
                .addString("targetDate", sunday.toString())
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(rankingMaterializeJob, params);
    }

    // 월간 마감: 매월 1일 — 전월 확정 스냅샷
    @Scheduled(cron = "0 15 0 1 * *", zone = "Asia/Seoul")
    public void monthlyClose() throws Exception {
        LocalDate first = LocalDate.now(KST).withDayOfMonth(1);
        LocalDate prevStart = first.minusMonths(1).withDayOfMonth(1);
        LocalDate prevEnd   = first.minusDays(1);

        JobParameters params = new JobParametersBuilder()
                .addString("fromDate",   prevStart.toString())
                .addString("toDate",     prevEnd.toString())
                .addString("targetDate", prevEnd.toString())
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(rankingMaterializeJob, params);
    }
}
