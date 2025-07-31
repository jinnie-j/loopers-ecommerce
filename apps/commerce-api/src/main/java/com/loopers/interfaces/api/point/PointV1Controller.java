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
    public ApiResponse<PointV1Dto.PointResponse> getPoint(
            @RequestHeader(value = "X-USER-ID")
            String userId
    ){
        if(userId == null || userId.isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        Optional<PointInfo> info = pointFacade.getPointInfo(userId);

        PointV1Dto.PointResponse response = PointV1Dto.PointResponse.from(info);
        return ApiResponse.success(response);
    }

    @PostMapping("/charge")
    @Override
    public ApiResponse<PointV1Dto.PointResponse> charge(
            @RequestHeader(value = "X-USER-ID") String userId,
            @RequestBody PointV1Dto.PointChargeRequest request
    ){

    PointInfo updatedPoint = pointFacade.chargePoint(userId, request.amount());
    PointV1Dto.PointResponse response = PointV1Dto.PointResponse.from(Optional.ofNullable(updatedPoint));
    return ApiResponse.success(response);
        }

}
