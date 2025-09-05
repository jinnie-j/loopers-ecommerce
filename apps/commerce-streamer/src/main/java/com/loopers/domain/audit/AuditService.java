package com.loopers.domain.audit;

import com.loopers.infrastructure.audit.EventLogJpaEntity;
import com.loopers.infrastructure.audit.EventLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final EventLogJpaRepository eventLogRepo;

    public void append(AuditCommand cmd) {
        eventLogRepo.save(EventLogJpaEntity.of(cmd.eventId(), cmd.topic(), cmd.key(), cmd.payloadJson()));
    }
}
