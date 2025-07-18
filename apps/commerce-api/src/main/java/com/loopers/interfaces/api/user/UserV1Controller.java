package com.loopers.interfaces.api.user;


import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.interfaces.api.ApiResponse;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserV1Controller implements UserV1ApiSpec {

    private final UserFacade userFacade;

    @PostMapping
    @Override
    public ApiResponse<UserV1Dto.UserResponse> signUp(
            @RequestBody UserV1Dto.SignUpRequest signUpRequest
    ){
        if(signUpRequest.gender() == null){
            throw new CoreException(ErrorType.BAD_REQUEST,"성별은 필수 입력값 입니다.");
        }
        return ApiResponse.success(
                new UserV1Dto.UserResponse(
                        "jinnie",
                        "지은",
                        "F",
                        "1997-01-27",
                        "jinnie@naver.com"
                )
        );
    }

    @GetMapping("/{userId}")
    @Override
    public ApiResponse<UserV1Dto.UserInfoResponse> getUserInfo(
            @PathVariable(value = "userId") String userId
    ) {
        UserInfo info = userFacade.getUserInfo(userId);
        UserV1Dto.UserInfoResponse response = UserV1Dto.UserInfoResponse.from(info);
        return ApiResponse.success(response);
    }
}
