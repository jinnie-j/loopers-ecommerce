package com.loopers.domain.user;

import com.loopers.domain.user.vo.Birth;
import com.loopers.domain.user.vo.Email;

public record UserInfo(Long id, String userId, String name, Email email, Gender gender, Birth birth) {
    public static UserInfo from(UserEntity userEntity){
        return new UserInfo(
                userEntity.getId(),
                userEntity.getUserId(),
                userEntity.getName(),
                userEntity.getEmail(),
                userEntity.getGender(),
                userEntity.getBirth()
        );
    }
}

