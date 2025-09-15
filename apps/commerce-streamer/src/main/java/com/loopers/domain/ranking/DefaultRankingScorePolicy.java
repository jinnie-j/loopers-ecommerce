package com.loopers.domain.ranking;

public class DefaultRankingScorePolicy implements RankingScorePolicy {
    private final double wView, wLike, wOrder;
    private final boolean logMode;

    public DefaultRankingScorePolicy(double wView, double wLike, double wOrder, boolean logMode) {
        this.wView = wView; this.wLike = wLike; this.wOrder = wOrder; this.logMode = logMode;
    }
    @Override public double viewDelta() { return wView * 1.0; }
    @Override public double likeDelta(int delta) { return wLike * delta; }
    @Override public double orderDelta(long price, long amount) {
        long base = price * amount;
        double v = logMode ? Math.log1p(base) : (double) base;
        return wOrder * v;
    }
}
