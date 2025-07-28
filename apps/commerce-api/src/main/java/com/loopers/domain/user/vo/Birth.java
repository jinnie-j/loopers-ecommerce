package com.loopers.domain.user.vo;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Getter
@Embeddable
public class Birth {
    private static final String PATTERN = "^\\d{4}-\\d{2}-\\d{2}$";

    private String value;

    protected Birth() {
    }

    public Birth(String birth) {
        if (birth == null || !birth.matches(PATTERN)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "생년월일은 'yyyy-MM-dd' 형식이어야 합니다.");
        }
        this.value = birth;
    }

    public static Birth of(String birth) {
        return new Birth(birth);
    }

    @Override
    public String toString() {
        return value;
    }
}
