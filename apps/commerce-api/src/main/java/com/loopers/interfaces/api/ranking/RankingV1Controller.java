package com.loopers.interfaces.api.ranking;

import com.loopers.application.ranking.RankingFacade;
import com.loopers.domain.ranking.PeriodType;
import com.loopers.domain.ranking.RankingProductPage;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class RankingV1Controller implements RankingV1ApiSpec {

    private final RankingFacade rankingFacade;

    @Override
    public ApiResponse<RankingPageResponse> page(
            @RequestParam(defaultValue = "DAY") PeriodType period,
            @RequestParam @DateTimeFormat(pattern="yyyyMMdd") LocalDate date,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "1") int page
    ) {
        RankingProductPage p = rankingFacade.getRankingPageWithProducts(period, date, page, size);
        return ApiResponse.success(new RankingPageResponse(
                p.page(), p.size(), p.total(),
                p.items().stream().map(RankingItemResponse::from).toList()
        ));
    }
}
