package com.loopers.infrastructure.product;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product_metrics")
public class ProductMetricsDailyEntity {

    @EmbeddedId
    private ProductMetricsDailyKey id;

    @Column(name = "like_delta", nullable = false)
    private long likeDelta;

    @Column(name = "view_delta", nullable = false)
    private long viewDelta;

    @Column(name = "sales_qty", nullable = false)
    private long salesQty;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static ProductMetricsDailyEntity of(long productId, java.time.LocalDate date,
                                               long viewDelta, long likeDelta, long salesQty, Instant updatedAt) {
        return ProductMetricsDailyEntity.builder()
                .id(new ProductMetricsDailyKey(date, productId))
                .viewDelta(viewDelta)
                .likeDelta(likeDelta)
                .salesQty(salesQty)
                .updatedAt(updatedAt)
                .build();
    }
}
