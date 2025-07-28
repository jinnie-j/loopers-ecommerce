package com.loopers.domain.user;

import java.util.Optional;

public interface UserRepository {
    Optional<UserEntity> findById(String userId);
    boolean existsByUserId(String userId);
    UserEntity save(UserEntity userEntity);
}
