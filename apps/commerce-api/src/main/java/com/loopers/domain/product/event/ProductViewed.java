package com.loopers.domain.product.event;

public record ProductViewed (
    long productId,
    Long viewerId
){}
