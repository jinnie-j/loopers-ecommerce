package com.loopers.infrastructure.product;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
@EqualsAndHashCode
public class ProductMetricsDailyKey implements Serializable {

    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;

    @Column(name = "product_id", nullable = false)
    private Long productId;

}
