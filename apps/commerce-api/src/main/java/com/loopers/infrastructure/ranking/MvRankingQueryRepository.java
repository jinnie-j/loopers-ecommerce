package com.loopers.infrastructure.ranking;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static com.loopers.domain.ranking.PeriodUtil.monthStart;
import static com.loopers.domain.ranking.PeriodUtil.weekStart;

@Repository
public class MvRankingQueryRepository {

    private final JdbcTemplate jdbc;
    public MvRankingQueryRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Item> weekly(LocalDate any, int offset, int size) {
        String key = weekStart(any).toString();
        return jdbc.query("""
            SELECT product_id, rank_no, score
            FROM mv_product_rank_weekly
            WHERE period_key=?
            ORDER BY rank_no ASC
            LIMIT ? OFFSET ?
        """, (rs,i) -> new Item(rs.getLong(1), rs.getInt(2), rs.getDouble(3)), key, size, offset);
    }
    public long weeklyTotal(LocalDate any) {
        String key = weekStart(any).toString();
        return jdbc.queryForObject("SELECT COUNT(*) FROM mv_product_rank_weekly WHERE period_key=?", Long.class, key);
    }

    public List<Item> monthly(LocalDate any, int offset, int size) {
        String key = monthStart(any).toString();
        return jdbc.query("""
            SELECT product_id, rank_no, score
            FROM mv_product_rank_monthly
            WHERE period_key=?
            ORDER BY rank_no ASC
            LIMIT ? OFFSET ?
        """, (rs,i) -> new Item(rs.getLong(1), rs.getInt(2), rs.getDouble(3)), key, size, offset);
    }
    public long monthlyTotal(LocalDate any) {
        String key = monthStart(any).toString();
        return jdbc.queryForObject("SELECT COUNT(*) FROM mv_product_rank_monthly WHERE period_key=?", Long.class, key);
    }

    public record Item(long productId, int rankNo, double score) {}
}
