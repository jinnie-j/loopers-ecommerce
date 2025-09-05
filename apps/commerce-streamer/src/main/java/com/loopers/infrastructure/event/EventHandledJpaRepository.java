package com.loopers.infrastructure.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventHandledJpaRepository extends JpaRepository<EventHandledJpaEntity, EventHandledKey> {}

