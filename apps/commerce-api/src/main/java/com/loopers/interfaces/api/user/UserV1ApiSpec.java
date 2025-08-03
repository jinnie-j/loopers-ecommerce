package com.loopers.interfaces.api.user;

import com.loopers.interfaces.api.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name ="User V1 API")
public interface UserV1ApiSpec {

    @Operation(summary = "회원 가입")
    ApiResponse<UserResponse> signUp(
            UserRequest signUpRequest
    );
    ApiResponse<UserResponse> getUserInfo(
            @Schema(name = "유저Id", description = "조회할 유저의 ID")
            String userId
    );
}
