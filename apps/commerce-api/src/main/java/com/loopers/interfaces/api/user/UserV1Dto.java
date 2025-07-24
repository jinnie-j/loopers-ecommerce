package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserInfo;
import jakarta.validation.constraints.NotNull;

public class UserV1Dto {
    public record SignUpRequest(
            @NotNull
            String userId,
            @NotNull
            String name,
            @NotNull
            String gender,
            @NotNull
            String birth,
            @NotNull
            String email
    ){
        public SignUpRequest{
        }
    }

    public record UserResponse(
            String userId,
            String name,
            String gender,
            String birth,
            String email
    ){}

    public record UserInfoResponse(String id, String name, String email) {
        public static UserV1Dto.UserInfoResponse from(UserInfo info) {
            return new UserV1Dto.UserInfoResponse(
                    info.userId(),
                    info.name(),
                    info.email()
            );
        }
    }
}
