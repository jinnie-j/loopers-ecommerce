package com.loopers.domain.user.vo;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.Getter;

@Getter
public class Birth {
    private static final String PATTERN = "^\\d{4}-\\d{2}-\\d{2}$";

    private final String value;

    public Birth(String birth) {
        if (birth == null || !birth.matches(PATTERN)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "생년월일은 'yyyy-MM-dd' 형식이어야 합니다.");
        }
        this.value = birth;
    }

    @Override
    public String toString() {
        return value;
    }
}
