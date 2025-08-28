package com.loopers.infrastructure.payment;
import com.loopers.domain.payment.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findByOrderId(Long orderId);
    Optional<PaymentEntity> findByPgTxId(String pgTxId);

    List<PaymentEntity> findByStatusInAndUpdatedAtBefore(
            @Param("statuses") Collection<PaymentStatus> statuses,
            @Param("before") ZonedDateTime before,
            Pageable pageable
    );

}
