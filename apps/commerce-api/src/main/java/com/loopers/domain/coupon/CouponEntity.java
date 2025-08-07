package com.loopers.domain.coupon;


import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "coupons")
@NoArgsConstructor
public class CouponEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private DiscountType disCountType;

    @Enumerated(EnumType.STRING)
    private CouponStatus couponStatus;

    private Long discountAmount;

    private Double discountRate;

    private LocalDateTime expiredAt;

    private CouponEntity(String name, DiscountType disCountType,  LocalDateTime expiredAt) {
        if (name == null || name.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰 이름은 필수입니다.");
        }
        if (disCountType == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "할인 정책은 필수입니다.");
        }
        if (couponStatus == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰 상태는 필수입니다.");
        }
        if (expiredAt == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰 만료일은 필수입니다.");
        }
        this.name = name;
        this.disCountType = DiscountType.FIXED_AMOUNT;
        this.couponStatus = CouponStatus.AVAILABLE;
        this.expiredAt = expiredAt;
    }
    public static CouponEntity of(String name, DiscountType disCountType, LocalDateTime expiredAt) {
        return new CouponEntity(name, disCountType, expiredAt);
    }

    public boolean isAvailable() {
        return CouponStatus.AVAILABLE.equals(this.couponStatus) && this.expiredAt.isBefore(LocalDateTime.now());
    }

    public long applyDiscount(long originalPrice){
        if(disCountType == DiscountType.FIXED_AMOUNT){
            return Math.max(0, originalPrice - discountAmount);
        }else if(disCountType == DiscountType.FIXED_RATE){
            return Math.max(0, (long)(originalPrice * (1-discountRate)));
        }
        return originalPrice;
    }
}
