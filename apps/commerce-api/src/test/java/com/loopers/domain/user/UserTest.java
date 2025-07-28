package com.loopers.domain.user;

import com.loopers.domain.user.vo.Birth;
import com.loopers.domain.user.vo.Email;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserTest {

    /*
    * 단위 테스트
    - [x]  ID 가 `영문 및 숫자 10자 이내` 형식에 맞지 않으면, User 객체 생성에 실패한다.
    - [x]  이메일이 `xx@yy.zz` 형식에 맞지 않으면, User 객체 생성에 실패한다.
    - [x]  생년월일이 `yyyy-MM-dd` 형식에 맞지 않으면, User 객체 생성에 실패한다.
     */
    @DisplayName("ID 가 `영문 및 숫자 10자 이내` 형식에 맞지 않으면, User 객체 생성에 실패한다.")
    @ParameterizedTest
    @ValueSource(strings ={
            "지은",
            "지지지지지지은은은은은",
            "jinn____ie",
            "12341234123",
            ""
    })
    void fail_whenIdFormatIsInvalid(String userId){
        // arrange
        final String name = "지은";
        final String email = "jinnie@naver.com";
        final String birth = "1997-01-27";
        final String gender = "FEMALE";

        // act
        var exception = assertThrows(CoreException.class, () -> {
            new UserEntity(
                    userId,
                    name,
                    Gender.valueOf(gender),
                    Birth.of(birth),
                    Email.of(email)
            );
        });
        // assert
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);

    }

    @DisplayName("이메일이 `xx@yy.zz` 형식에 맞지 않으면, User 객체 생성에 실패한다.")
    @Test
    void fail_whenNameFormatIsInvalid(){
        //arrange
        final String userId = "jinnie";
        final String name = "지은";
        final String email = "jinnie@naver";
        final String birth = "1997-01-27";
        final String gender = "FEMALE";

        //act
        final CoreException exception = assertThrows(CoreException.class, () -> {
            new UserEntity(
                    userId,
                    name,
                    Gender.valueOf(gender),
                    Birth.of(birth),
                    Email.of(email)
            );
        });
        //assert
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);

    }

    @DisplayName("생년월일이 `yyyy-MM-dd` 형식에 맞지 않으면, User 객체 생성에 실패한다.")
    @Test
    void fail_whenBirthDateFormatIsInvalid(){
        //arrange
        final String userId = "jinnie";
        final String name = "지은";
        final String email = "jinnie@naver";
        final String birth = "1997-01";
        final String gender = "FEMALE";

        //act
        final CoreException exception = assertThrows(CoreException.class, () -> {
            new UserEntity(
                    userId,
                    name,
                    Gender.valueOf(gender),
                    Birth.of(birth),
                    Email.of(email)
            );
        });
        //assert
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
    }
}
