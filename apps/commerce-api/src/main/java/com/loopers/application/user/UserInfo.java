package com.loopers.application.user;

import com.loopers.domain.user.Gender;
import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.vo.Birth;
import com.loopers.domain.user.vo.Email;

public record UserInfo(String userId, String name, Email email, Gender gender, Birth birth) {
    public static UserInfo from(UserEntity userEntity){
        return new UserInfo(
                userEntity.getUserId(),
                userEntity.getName(),
                userEntity.getEmail(),
                userEntity.getGender(),
                userEntity.getBirth()
        );
    }
}

