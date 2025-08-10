package com.loopers.interfaces.api.point;

import com.loopers.interfaces.api.ApiResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name ="Point V1 API")
@RequestMapping("/api/v1/points")
public interface PointV1ApiSpec {

    ApiResponse<PointResponse> getPoint(
            @Schema(name = "X-USER-ID")
            Long userId
    );

    @PostMapping("/charge")
    ApiResponse<PointResponse> charge(
            @Schema(name = "X-USER-ID") Long userId,
            @RequestBody PointRequest.PointChargeRequest request
    );
}
