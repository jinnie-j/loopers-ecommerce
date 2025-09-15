package com.loopers.domain.ranking;

public interface RankingScorePolicy {
    double viewDelta();                        // w_view * 1
    double likeDelta(int delta);               // w_like * (+1/-1)
    double orderDelta(long price, long amount);// w_order * (raw or log1p)
}
