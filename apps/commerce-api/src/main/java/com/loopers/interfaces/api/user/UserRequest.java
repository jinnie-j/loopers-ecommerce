package com.loopers.interfaces.api.user;

import com.loopers.domain.user.Gender;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.vo.Birth;
import com.loopers.domain.user.vo.Email;
import jakarta.validation.constraints.NotNull;

public record UserRequest(
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
