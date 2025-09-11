package com.loopers.domain.product.event;

public record ProductLiked(long productId, Long userId, int delta) {}
