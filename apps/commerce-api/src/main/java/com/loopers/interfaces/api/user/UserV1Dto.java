package com.loopers.interfaces.api.user;

import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserInfo;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.vo.Birth;
import com.loopers.domain.user.vo.Email;
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
    ) {
        public UserCommand.SignUp toCommand() {
            return new UserCommand.SignUp(userId, name, Gender.valueOf(gender), Birth.of(birth), Email.of(email));
        }
    }

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
}
