package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    @Override public PaymentEntity save(PaymentEntity entity) { return paymentJpaRepository.save(entity); }
    @Override public Optional<PaymentEntity> findByOrderId(Long orderId) { return paymentJpaRepository.findByOrderId(orderId); }
    @Override public Optional<PaymentEntity> findByPgTxId(String pgTxId) { return paymentJpaRepository.findByPgTxId(pgTxId); }
}

