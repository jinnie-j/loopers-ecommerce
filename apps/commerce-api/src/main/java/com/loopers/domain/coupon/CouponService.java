package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.loopers.support.error.ErrorType.BAD_REQUEST;
import static com.loopers.support.error.ErrorType.NOT_FOUND;

@RequiredArgsConstructor
@Service
public class CouponService {
    private final CouponRepository couponRepository;

    @Transactional
    public CouponInfo create(CouponCommand.Create command) {
        CouponEntity coupon = CouponEntity.of(
                command.name(), command.discountType(), command.discountAmount(), command.discountRate(),command.expiredAt()
        );
        return CouponInfo.from(couponRepository.save(coupon));
    }

    @Transactional
    public CouponEntity getAvailableCoupon(Long couponId) {
        CouponEntity coupon = couponRepository.findWithLockById(couponId)
                .orElseThrow(() -> new CoreException(NOT_FOUND, "존재하지 않는 쿠폰입니다."));
        if (!coupon.isAvailable()) throw new CoreException(BAD_REQUEST, "사용이 불가한 쿠폰입니다.");
        return coupon;
    }

    public long applyDiscount(CouponEntity coupon, long price) {
        return coupon.applyDiscount(price);
    }
}
