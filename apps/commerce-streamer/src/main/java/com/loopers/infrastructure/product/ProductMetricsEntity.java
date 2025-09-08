package com.loopers.infrastructure.product;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name="product_metrics_snapshot")
@Getter
@NoArgsConstructor
public class ProductMetricsEntity {
    @Id
    private Long productId;
    private long likeCount;
    private Instant lastUpdatedAt;

    private ProductMetricsEntity(Long productId, long likeCount, Instant lastUpdatedAt) {
        this.productId = productId;
        this.likeCount = likeCount;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public static ProductMetricsEntity of(Long productId, long likeCount, Instant updatedAt) {
        return new ProductMetricsEntity(productId, likeCount, updatedAt);
    }

    public void apply(long likeCount, Instant updatedAt){
        if (lastUpdatedAt!=null && updatedAt.isBefore(lastUpdatedAt)) return;
        this.likeCount = likeCount; this.lastUpdatedAt = updatedAt;
    }
}
