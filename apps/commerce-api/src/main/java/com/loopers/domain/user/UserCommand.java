package com.loopers.domain.user;

import com.loopers.domain.user.vo.Birth;
import com.loopers.domain.user.vo.Email;

public class UserCommand {

    public record SignUp(
            String userId,
            String name,
            Gender gender,
            Birth birth,
            Email email
    ) {}
}
