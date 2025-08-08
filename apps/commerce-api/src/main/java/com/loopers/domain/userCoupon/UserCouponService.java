package com.loopers.domain.userCoupon;

import com.loopers.support.error.CoreException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.loopers.support.error.ErrorType.BAD_REQUEST;
import static com.loopers.support.error.ErrorType.NOT_FOUND;

@RequiredArgsConstructor
@Service
@Transactional
public class UserCouponService {
    private final UserCouponRepository userCouponRepository;


    public void useCoupon(UserCouponCommand.Use command) {
        UserCouponEntity userCoupon = userCouponRepository
                .findWithLockByUserIdAndCouponId(command.userId(), command.couponId()) // 비관적 락
                .orElseThrow(() -> new CoreException(NOT_FOUND, "소유하지 않은 쿠폰입니다."));

        if (userCoupon.isUsed()) {
            throw new CoreException(BAD_REQUEST, "이미 사용된 쿠폰입니다.");
        }

        userCoupon.use();
    }
}
