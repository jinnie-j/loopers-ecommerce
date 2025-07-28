package com.loopers.domain.user.vo;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.regex.Pattern;

@Getter
@Embeddable
public class Email {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    private String value;

    public Email() {
    }
    public Email(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new CoreException(
                    ErrorType.BAD_REQUEST,
                    "이메일은 xx@yy.zz 형식이어야 합니다."
            );
        }
        this.value = email;
    }


    public static Email of(String email) {
        return new Email(email);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Email other)) return false;
        return value.equalsIgnoreCase(other.value);
    }

    @Override
    public int hashCode() {
        return value.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
