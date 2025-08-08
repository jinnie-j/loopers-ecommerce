package com.loopers.interfaces.api.user;


import com.loopers.application.user.UserFacade;
import com.loopers.domain.user.UserInfo;
import com.loopers.interfaces.api.ApiResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class UserV1Controller implements UserV1ApiSpec {

    private final UserFacade userFacade;

    @PostMapping
    @Override
    public ApiResponse<UserResponse> signUp(@RequestBody UserRequest signUpRequest
    ){
        final  UserResponse response = UserResponse.from(userFacade.signUp(signUpRequest.toCommand()));
        return ApiResponse.success(response);
    }

    @Override
    public ApiResponse<UserResponse> getUserInfo(
            @PathVariable(value = "userId") String userId
    ) {
        UserInfo info = userFacade.getUserInfo(userId);
        UserResponse response = UserResponse.from(info);
        return ApiResponse.success(response);
    }
}
