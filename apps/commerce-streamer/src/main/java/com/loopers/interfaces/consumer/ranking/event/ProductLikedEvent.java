package com.loopers.interfaces.consumer.ranking.event;

public record ProductLikedEvent(long productId, Long userId, int delta) {}
