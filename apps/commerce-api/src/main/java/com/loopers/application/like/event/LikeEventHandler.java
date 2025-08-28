package com.loopers.application.like.event;

import com.loopers.domain.like.event.LikeChangedEvent;
import com.loopers.infrastructure.product.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class LikeEventHandler {

    private final ProductJpaRepository productJpaRepository;

    @Async("appExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onLikeChanged(LikeChangedEvent e) {
        try {
            if (e.delta() > 0) productJpaRepository.incrementLikeCount(e.productId());
            else               productJpaRepository.decrementLikeCount(e.productId());
        } catch (Exception ex) {
            log.warn("failed: productId={}, delta={}, err={}",
                    e.productId(), e.delta(), ex.toString());
        }
    }
}

