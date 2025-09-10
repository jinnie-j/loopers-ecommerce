package com.loopers.interfaces.api.ranking;

import com.loopers.domain.ranking.RankingPage;
import com.loopers.interfaces.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/v1/rankings")
public interface RankingV1ApiSpec {
    @GetMapping
    ApiResponse<RankingPage> page(@RequestParam String date,
                                  @RequestParam(defaultValue = "20") int size,
                                  @RequestParam(defaultValue = "1") int page);
}
