package com.loopers.infrastructure.writer;

import com.loopers.domain.ranking.PeriodAccumulatedScore;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StageScoreItemWriter implements ItemWriter<PeriodAccumulatedScore> {

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public void write(Chunk<? extends PeriodAccumulatedScore> chunk) {
        if (chunk == null || chunk.isEmpty()) return;

        String sql = """
            INSERT INTO mv_stage_scores(period_type, period_key, product_id, score)
            VALUES(:t, :k, :p, :s)
            ON DUPLICATE KEY UPDATE score = score + VALUES(score)
        """;

        List<Map<String, Object>> batch = new ArrayList<>(chunk.size() * 2);
        for (PeriodAccumulatedScore it : chunk.getItems()) {
            batch.add(Map.of("t","W","k",it.weekKey(),"p",it.productId(),"s",it.score()));
            batch.add(Map.of("t","M","k",it.monthKey(),"p",it.productId(),"s",it.score()));
        }
        jdbc.batchUpdate(sql, batch.toArray(new Map[0]));
    }
}
