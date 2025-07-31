package com.loopers.domain.user;


import com.loopers.domain.user.vo.Birth;
import com.loopers.domain.user.vo.Email;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class UserEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userId;
    private String name;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email"))
    private Email email;
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "birth"))
    private Birth birth;

    private final String PATTERN_USER_ID = "^[a-zA-Z0-9]{1,10}$";
    private final String PATTERN_EMAIL = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
    private final String PATTERN_BIRTH = "^\\d{4}-\\d{2}-\\d{2}$";

    public UserEntity(
            String userId,
            String name,
            Gender gender,
            Birth birth,
            Email email

    ){
        if(userId == null || !userId.matches(PATTERN_USER_ID)){
            throw new CoreException(
                    ErrorType.BAD_REQUEST,
                    "ID는 영문 및 숫자 10자 이내로 입력해야 합니다."
            );
        }

        this.userId = userId;
        this.name = name;
        this.gender = gender;
        this.birth = birth;
        this.email = email;
    }

}
