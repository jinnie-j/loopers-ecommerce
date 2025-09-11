package com.loopers.application.ranking;

import com.loopers.domain.ranking.RankingItem;
import com.loopers.domain.ranking.RankingPage;
import com.loopers.domain.ranking.RankingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RankingFacadeIntegrationTest {
    @Mock RankingRepository rankingRepository;

    @Test
    @DisplayName("총 개수가 0이면 빈 페이지를 반환한다")
    void getRankingPage_empty_when_total_zero() {
        RankingFacade sut = new RankingFacade(rankingRepository);
        when(rankingRepository.size(anyString())).thenReturn(0L);

        RankingPage page = sut.getRankingPage(LocalDate.of(2025, 9, 11), 1, 10);

        assertThat(page.total()).isEqualTo(0);
        assertThat(page.items()).isEmpty();
        assertThat(page.page()).isEqualTo(1);
        assertThat(page.size()).isEqualTo(10);

        verify(rankingRepository, times(1)).size(anyString());
        verify(rankingRepository, never()).reverseRangeWithScores(anyString(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("내림차순 스코어를 순위로 매핑해 페이지를 구성한다")
    void getRankingPage_maps_member_scores_to_rank_items() {
        RankingFacade sut = new RankingFacade(rankingRepository);

        when(rankingRepository.size(anyString())).thenReturn(3L);
        when(rankingRepository.reverseRangeWithScores(anyString(), eq(0L), eq(1L)))
                .thenReturn(List.of(
                        new RankingRepository.MemberScore(101L, 7.5),
                        new RankingRepository.MemberScore(202L, 3.2)
                ));

        RankingPage page = sut.getRankingPage(LocalDate.of(2025, 9, 11), 1, 2);

        assertThat(page.total()).isEqualTo(3);
        assertThat(page.page()).isEqualTo(1);
        assertThat(page.size()).isEqualTo(2);
        assertThat(page.items()).hasSize(2);

        RankingItem first = page.items().get(0);
        assertThat(first.productId()).isEqualTo(101L);
        assertThat(first.score()).isEqualTo(7.5);
        assertThat(first.rank()).isEqualTo(1L);

        RankingItem second = page.items().get(1);
        assertThat(second.productId()).isEqualTo(202L);
        assertThat(second.score()).isEqualTo(3.2);
        assertThat(second.rank()).isEqualTo(2L);

        verify(rankingRepository).size(anyString());
        verify(rankingRepository).reverseRangeWithScores(anyString(), eq(0L), eq(1L));
        verifyNoMoreInteractions(rankingRepository);
    }

    @Test
    @DisplayName("오늘의 단건 랭킹이 존재하면 0-based rank를 +1하여 반환한다")
    void getTodayRankOf_present() {
        RankingFacade sut = new RankingFacade(rankingRepository);

        when(rankingRepository.reverseRank(anyString(), eq(555L))).thenReturn(Optional.of(0L));
        when(rankingRepository.score(anyString(), eq(555L))).thenReturn(Optional.of(9.99));

        Optional<RankingItem> opt = sut.getTodayRankOf(555L);

        assertThat(opt).isPresent();
        RankingItem item = opt.get();
        assertThat(item.productId()).isEqualTo(555L);
        assertThat(item.rank()).isEqualTo(1L);
        assertThat(item.score()).isEqualTo(9.99);

        verify(rankingRepository).reverseRank(anyString(), eq(555L));
        verify(rankingRepository).score(anyString(), eq(555L));
        verifyNoMoreInteractions(rankingRepository);
    }

    @Test
    @DisplayName("오늘의 단건 랭킹이 없으면 빈 Optional을 반환한다")
    void getTodayRankOf_absent() {
        RankingFacade sut = new RankingFacade(rankingRepository);

        when(rankingRepository.reverseRank(anyString(), eq(42L))).thenReturn(Optional.empty());

        Optional<RankingItem> opt = sut.getTodayRankOf(42L);

        assertThat(opt).isEmpty();

        verify(rankingRepository).reverseRank(anyString(), eq(42L));
        verify(rankingRepository, never()).score(anyString(), anyLong());
    }
}
