package com.loopers.domain.payment;

import java.util.Optional;

public interface PaymentRepository{
    PaymentEntity save(PaymentEntity entity);
    Optional<PaymentEntity> findByOrderId(Long orderId);
    Optional<PaymentEntity> findByPgTxId(String pgTxId);
}
