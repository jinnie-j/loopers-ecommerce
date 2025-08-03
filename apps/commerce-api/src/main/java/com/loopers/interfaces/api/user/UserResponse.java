package com.loopers.interfaces.api.user;

import com.loopers.domain.user.UserInfo;

public record UserResponse(
        String userId,
        String name,
        String gender,
        String birth,
        String email
) {

    public static UserResponse from(UserInfo userInfo) {
        return new UserResponse(
                userInfo.userId(),
                userInfo.name(),
                userInfo.gender().name(),
                userInfo.birth().getValue(),
                userInfo.email().getValue()
        );
    }
}
