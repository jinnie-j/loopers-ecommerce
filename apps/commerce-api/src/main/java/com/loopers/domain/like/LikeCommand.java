package com.loopers.domain.like;

public class LikeCommand {
    public record Create(long userId, long productId) {
    }
}
