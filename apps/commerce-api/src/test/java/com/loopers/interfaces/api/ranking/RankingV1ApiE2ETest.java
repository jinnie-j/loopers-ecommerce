package com.loopers.interfaces.api.ranking;

import com.loopers.application.ranking.RankingFacade;
import com.loopers.domain.ranking.RankingItem;
import com.loopers.domain.ranking.RankingPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Ranking API E2E 테스트")
@WebMvcTest(RankingV1Controller.class)
public class RankingV1ApiE2ETest {

    @Autowired MockMvc mvc;
    @MockitoBean RankingFacade rankingFacade;

    @Test
    @DisplayName("랭킹 페이지를 JSON으로 반환한다")
    void getRankings_jsonContract() throws Exception {
        var items = List.of(
                new RankingItem(101L, 9.9, 1L),
                new RankingItem(202L, 7.7, 2L)
        );
        var page = new RankingPage(items, 1, 2, 10);
        when(rankingFacade.getRankingPage(any(LocalDate.class), eq(1), eq(2)))
                .thenReturn(page);

        mvc.perform(get("/api/v1/rankings")
                        .param("date", "20250911")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.total").value(10))
                .andExpect(jsonPath("$.data.items[0].productId").value(101))
                .andExpect(jsonPath("$.data.items[0].rank").value(1))
                .andExpect(jsonPath("$.data.items[0].score").value(9.9));
    }
}
