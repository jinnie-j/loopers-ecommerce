package com.loopers.interfaces.api.point;

import com.loopers.application.point.PointFacade;
import com.loopers.domain.point.PointInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/points")
public class PointV1Controller implements PointV1ApiSpec{

    private final PointFacade pointFacade;

    @GetMapping
    @Override
    public ApiResponse<PointResponse> getPoint(
            @RequestHeader(value = "X-USER-ID")
            Long userId
    ){
        if(userId == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }

        PointInfo info = pointFacade.getPointInfo(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "포인트 정보를 찾을 수 없습니다."));

        PointResponse response = PointResponse.from(info);
        return ApiResponse.success(response);
    }

    @Override
    public ApiResponse<PointResponse> charge(
            @RequestHeader(value = "X-USER-ID") Long userId,
            @RequestBody PointRequest.PointChargeRequest request
    ){
        PointInfo updatedPoint = pointFacade.chargePoint(userId, request.amount());

        PointResponse response = PointResponse.from(updatedPoint);
        return ApiResponse.success(response);
        }


}
