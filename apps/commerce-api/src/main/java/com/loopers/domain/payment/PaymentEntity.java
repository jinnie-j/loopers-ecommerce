package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor
public class PaymentEntity extends BaseEntity {

    @Id @GeneratedValue
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "pg_tx_id", unique = true)
    private String pgTxId;

    @Column(name = "coupon_id")
    private Long couponId;

    private PaymentEntity(Long orderId, Long userId, Long amount, PaymentMethod method, Long couponId) {
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.method = method;
        this.couponId = couponId;
        this.status = PaymentStatus.REQUESTED;
    }

    public static PaymentEntity request(Long orderId, Long userId, Long amount, PaymentMethod method, Long couponId) {
        return new PaymentEntity(orderId, userId, amount, method, couponId);
    }

    public void markPending(String txId) {
        this.pgTxId = txId;
        this.status = PaymentStatus.PENDING;
    }

    public void approve() { this.status = PaymentStatus.APPROVED; }
    public void decline() { this.status = PaymentStatus.DECLINED; }
    public void fail() { this.status = PaymentStatus.FAILED; }
}
