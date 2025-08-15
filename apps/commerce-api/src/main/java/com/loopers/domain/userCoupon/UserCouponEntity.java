package com.loopers.domain.userCoupon;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCouponEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long couponId;

    @Enumerated(EnumType.STRING)
    private UserCouponStatus status;

    private UserCouponEntity(Long userId, Long couponId) {
        this.userId = userId;
        this.couponId = couponId;
        this.status = UserCouponStatus.ISSUED;
    }

    public static UserCouponEntity of(Long userId, Long couponId){
        return new UserCouponEntity(userId, couponId);
    }

    public void use() {
        if (this.status != UserCouponStatus.ISSUED) {
            throw new IllegalStateException("사용할 수 없는 쿠폰 상태입니다.");
        }
        this.status = UserCouponStatus.USED;
    }

    public boolean isUsed() {
        return this.status == UserCouponStatus.USED;
    }
}
