package com.loopers.domain.user;

import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.interfaces.api.user.UserV1Dto;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserJpaRepository userJpaRepository;

    @Transactional(readOnly = true)
    public UserEntity getUserEntity(String userId) {
        return userJpaRepository.findById(userId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "[id = " + userId + "] 회원을 찾을 수 없습니다."));
    }

    @Transactional
    public UserV1Dto.UserResponse signUp(UserV1Dto.SignUpRequest request) {
        // 1. 중복 userId 체크
        boolean exists = userJpaRepository.existsByUserId(request.userId());
        if (exists) {
            throw new CoreException(ErrorType.CONFLICT, "이미 존재하는 사용자 ID입니다.");
        }

        // 2. UserEntity 생성
        UserEntity userEntity = new UserEntity(
                request.userId(),
                request.name(),
                request.gender(),
                request.birth(),
                request.email()
        );

        // 3. 저장
        UserEntity savedUser = userJpaRepository.save(userEntity);

        // 4. DTO 반환
        return new UserV1Dto.UserResponse(
                savedUser.getUserId(),
                savedUser.getName(),
                savedUser.getGender(),
                savedUser.getBirth(),
                savedUser.getEmail()
        );
    }
}
