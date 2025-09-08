package com.loopers.infrastructure.audit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventLogJpaRepository extends JpaRepository<EventLogJpaEntity, Long> {
}
