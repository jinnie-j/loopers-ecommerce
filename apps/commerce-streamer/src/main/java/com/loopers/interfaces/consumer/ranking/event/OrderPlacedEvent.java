package com.loopers.interfaces.consumer.ranking.event;


import java.util.List;
public record OrderPlacedEvent(String orderId, List<Item> items) {
    public record Item(long productId, long price, long amount) {}
}
