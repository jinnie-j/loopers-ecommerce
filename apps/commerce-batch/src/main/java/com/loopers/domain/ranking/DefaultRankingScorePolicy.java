package com.loopers.domain.ranking;

public class DefaultRankingScorePolicy implements RankingScorePolicy {
    private final double wView, wLike, wOrder;
    private final boolean useLog1p;

    public DefaultRankingScorePolicy(double wView, double wLike, double wOrder, boolean useLog1p) {
        this.wView = wView;
        this.wLike = wLike;
        this.wOrder = wOrder;
        this.useLog1p = useLog1p;
    }

    @Override
    public double score(long views, long likes, long orderAmount, long orderQty) {
        return wView  * val(views)
                + wLike  * val(likes)
                + wOrder * val(orderQty);
    }

    private double val(long x) {
        return useLog1p ? Math.log1p(Math.max(0, x)) : (double) Math.max(0, x);
    }
}
