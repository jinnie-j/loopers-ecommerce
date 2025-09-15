package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByUserId(@Param("userId") Long userId);

    @Query("select o from OrderEntity o left join fetch o.orderItems where o.id = :id")
    Optional<OrderEntity> findByIdWithItems(@Param("id") Long id);
}
