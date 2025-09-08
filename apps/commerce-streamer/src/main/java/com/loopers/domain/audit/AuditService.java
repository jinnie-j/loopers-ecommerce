package com.loopers.domain.audit;

import com.loopers.infrastructure.audit.EventLogJpaEntity;
import com.loopers.infrastructure.audit.EventLogJpaRepository;
import com.loopers.infrastructure.event.EventHandledJpaEntity;
import com.loopers.infrastructure.event.EventHandledJpaRepository;
import com.loopers.infrastructure.event.EventHandledKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final EventLogJpaRepository eventLogRepo;
    private final EventHandledJpaRepository handledRepo;
    private static final String HANDLER = "AUDIT";

    public void appendIfFirst(AuditCommand cmd) {
        var key = new EventHandledKey(cmd.eventId(), HANDLER);
        if (handledRepo.existsById(key)) return;
        eventLogRepo.save(EventLogJpaEntity.of(cmd.eventId(), cmd.topic(), cmd.key(), cmd.payloadJson()));
        handledRepo.save(EventHandledJpaEntity.of(cmd.eventId(), HANDLER));
    }
}
