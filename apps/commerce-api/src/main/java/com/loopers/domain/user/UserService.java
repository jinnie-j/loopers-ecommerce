package com.loopers.domain.user;

import com.loopers.application.user.UserCommand;
import com.loopers.application.user.UserInfo;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserEntity getUserEntity(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "[id = " + userId + "] 회원을 찾을 수 없습니다."));
    }

    @Transactional
    public UserInfo signUp(UserCommand.SignUp command) {

        if(userRepository.existsByUserId(command.userId())) {
            throw new CoreException(ErrorType.CONFLICT, "이미 존재하는 사용자 ID입니다.");
        }

        UserEntity userEntity = new UserEntity(
                command.userId(),
                command.name(),
                command.gender(),
                command.birth(),
                command.email()
        );
        return UserInfo.from(userRepository.save(userEntity));
    }
}
