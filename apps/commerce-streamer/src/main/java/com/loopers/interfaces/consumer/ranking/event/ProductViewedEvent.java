package com.loopers.interfaces.consumer.ranking.event;

public record ProductViewedEvent(long productId, Long viewerId) {}
