package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductEntity p WHERE p.id = :id")
    Optional<ProductEntity> findWithLockById(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ProductEntity p set p.likeCount = p.likeCount + 1 where p.id = :productId")
    int incrementLikeCount(@Param("productId") Long productId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update ProductEntity p
           set p.likeCount = case when p.likeCount > 0 then p.likeCount - 1 else 0 end
           where p.id = :productId
           """)
    int decrementLikeCount(@Param("productId") Long productId);
}
