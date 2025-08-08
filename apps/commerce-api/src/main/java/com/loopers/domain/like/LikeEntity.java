package com.loopers.domain.like;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "likes",
        uniqueConstraints = @UniqueConstraint(name = "uk_like_user_product", columnNames = {"user_id", "product_id"})
)
@Getter
@NoArgsConstructor
public class LikeEntity extends BaseEntity {

    private Long userId;
    private Long productId;

    private LikeEntity(Long userId, Long productId) {
        if(userId == null || productId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        this.userId = userId;
        this.productId = productId;
    }

    public static LikeEntity of(Long userId, Long productId) {
        return new LikeEntity(userId, productId);
    }
}
