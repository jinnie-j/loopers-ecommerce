package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    @Override public PaymentEntity save(PaymentEntity entity) { return paymentJpaRepository.save(entity); }
    @Override public Optional<PaymentEntity> findByOrderId(Long orderId) { return paymentJpaRepository.findByOrderId(orderId); }
    @Override public Optional<PaymentEntity> findByPgTxId(String pgTxId) { return paymentJpaRepository.findByPgTxId(pgTxId); }
    @Override
    public List<PaymentEntity> findReconTargets(LocalDateTime cutoff, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(ASC, "updatedAt"));
        return paymentJpaRepository.findByStatusInAndUpdatedAtBefore(
                List.of(PaymentStatus.PENDING, PaymentStatus.REQUESTED),
                cutoff,
                pageable
        );
    }
}

