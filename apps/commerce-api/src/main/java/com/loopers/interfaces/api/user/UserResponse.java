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
        String gender = switch (userInfo.gender()){
            case FEMALE -> "F";
            case MALE -> "M";
            default -> null;
        };
        return new UserResponse(
                userInfo.userId(),
                userInfo.name(),
                gender,
                userInfo.birth().getValue(),
                userInfo.email().getValue()
        );
    }
}
