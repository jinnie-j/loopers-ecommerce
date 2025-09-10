package com.loopers.interfaces.api.ranking;

import com.loopers.application.ranking.RankingFacade;
import com.loopers.domain.ranking.RankingPage;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequiredArgsConstructor
public class RankingV1Controller implements RankingV1ApiSpec {

    private final RankingFacade facade;

    @Override
    public ApiResponse<RankingPage> page(String date, int size, int page) {
        LocalDate d = LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd
        return ApiResponse.success(facade.getRankingPage(d, page, size));
    }
}
