package com.loopers.infrastructure.user;

import com.loopers.domain.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserEntity, String> {
    boolean existsByUserId(String userId);
}
