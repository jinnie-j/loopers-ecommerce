package com.loopers.interfaces.api.point;

import com.loopers.interfaces.api.ApiResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name ="Point V1 API")
public interface PointV1ApiSpec {

    ApiResponse<PointV1Dto.PointResponse> getPoint(
            @Schema(name = "유저 ID", description = "포인트를 조회할 유저의 ID")
            Long userId
    );

    @PostMapping("/charge")
    ApiResponse<PointV1Dto.PointResponse> charge(
            @Schema(name = "X-USER-ID") Long userId,
            @RequestBody PointV1Dto.PointChargeRequest request
    );
}
