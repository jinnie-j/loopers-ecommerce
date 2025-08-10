package com.loopers.interfaces.api.user;

import com.loopers.domain.user.Gender;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.vo.Birth;
import com.loopers.domain.user.vo.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequest(
        @NotBlank
        String userId,
        @NotBlank
        String name,
        @NotBlank
        String gender,
        @NotBlank
        String birth,
        @NotBlank
        String email
) {
    public UserCommand.SignUp toCommand() {
        return new UserCommand.SignUp(userId, name, Gender.from(gender), Birth.of(birth), Email.of(email));
    }
}
