package com.loopers.infrastructure.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

@Repository
public interface ProductMetricsDailyJpaRepository extends JpaRepository<ProductMetricsDailyEntity, ProductMetricsDailyKey> {

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO product_metrics(metric_date, product_id, like_delta, sales_qty, updated_at)
        VALUES (:metricDate, :productId, :likeDelta, :salesQty, :updatedAt)
        ON DUPLICATE KEY UPDATE
          like_delta = like_delta + VALUES(like_delta),
          sales_qty  = sales_qty  + VALUES(sales_qty),
          updated_at = GREATEST(updated_at, VALUES(updated_at))
        """, nativeQuery = true)
    void upsertDaily(
            @Param("metricDate") LocalDate metricDate,
            @Param("productId") long productId,
            @Param("likeDelta") long likeDelta,
            @Param("salesQty") long salesQty,
            @Param("updatedAt") Instant updatedAt
    );
}
