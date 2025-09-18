package com.loopers.infrastructure.reader;

import com.loopers.domain.ranking.ProductDailyMetric;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class ProductMetricsReader {

    private final DataSource dataSource;

    @Bean
    @StepScope
    public JdbcPagingItemReader<ProductDailyMetric> pmReader(
            @Value("#{jobParameters['fromDate']}") String fromDate,
            @Value("#{jobParameters['toDate']}")   String toDate
    ) {
        Map<String, Object> params = Map.of(
                "fromDate", java.sql.Date.valueOf(fromDate),
                "toDate",   java.sql.Date.valueOf(toDate)
        );

        return new JdbcPagingItemReaderBuilder<ProductDailyMetric>()
                .name("pmReader")
                .dataSource(dataSource)
                .selectClause("SELECT metric_date, product_id, like_delta, sales_qty")
                .fromClause("FROM product_metrics")
                .whereClause("WHERE metric_date BETWEEN :fromDate AND :toDate")
                .parameterValues(params)
                .sortKeys(Map.of(
                        "metric_date", Order.ASCENDING,
                        "product_id",  Order.ASCENDING
                ))
                .rowMapper(rowMapper())
                .pageSize(2_000)
                .build();
    }

    private RowMapper<ProductDailyMetric> rowMapper() {
        return (ResultSet rs, int rowNum) -> new ProductDailyMetric(
                rs.getDate("metric_date").toLocalDate(),
                rs.getLong("product_id"),
                rs.getLong("view_delta"),
                rs.getLong("like_delta"),
                rs.getLong("sales_qty")
        );
    }
}
