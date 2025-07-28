package com.loopers.application.user;


import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserFacade {
    private final UserService userService;

    public UserInfo getUserInfo(String userId){
        UserEntity userEntity = userService.getUserEntity(userId);
                return UserInfo.from(userEntity);
    }

    public UserInfo signUp(UserCommand.SignUp command) {
        return userService.signUp(command);
    }
}
