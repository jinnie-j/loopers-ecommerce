package com.loopers.application.user;

import com.loopers.domain.user.UserEntity;

public record UserInfo(String userId, String name, String email){
    public static UserInfo from(UserEntity userEntity){
        return new UserInfo(
                userEntity.getUserId(),
                userEntity.getName(),
                userEntity.getUserId()
        );
    }
}

