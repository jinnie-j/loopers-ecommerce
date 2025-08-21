package com.loopers.domain.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentTest {

    @Test
    @DisplayName("초기 상태는 REQUESTED이고 pgTxId는 비어 있다")
    void request_initialState() {
        // arrange & act
        var payment = PaymentEntity.request(
                1001L,
                42L,
                50_000L,
                PaymentMethod.CARD,
                null
        );

        // assert
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REQUESTED);
        assertThat(payment.getPgTxId()).isNull();
    }

    @Test
    @DisplayName("PG 트랜잭션 ID를 세팅하고 상태를 PENDING으로 만든다.")
    void markPending_setsPgTxAndPending() {
        var payment = PaymentEntity.request(1L, 1L, 10_000L, PaymentMethod.CARD, null);

        payment.markPending("TX-123");

        assertThat(payment.getPgTxId()).isEqualTo("TX-123");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("상태를 APPROVED로 만든다.")
    void approve_changesToApproved() {
        var payment = PaymentEntity.request(1L, 1L, 10_000L, PaymentMethod.CARD, null);
        payment.markPending("TX-1");

        payment.approve();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
    }

    @Test
    @DisplayName("상태를 DECLINED로 만든다.")
    void decline_changesToDeclined() {
        var payment = PaymentEntity.request(1L, 1L, 10_000L, PaymentMethod.CARD, null);
        payment.markPending("TX-1");

        payment.decline();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.DECLINED);
    }

    @Test
    @DisplayName("상태를 FAILED로 만든다.(타임아웃/5xx 등 실패 케이스)")
    void fail_changesToFailed() {
        var payment = PaymentEntity.request(1L, 1L, 10_000L, PaymentMethod.CARD, null);

        payment.fail();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    @DisplayName("승인 과정: REQUESTED → PENDING → APPROVED")
    void lifecycle_requested_pending_approved() {
        var payment = PaymentEntity.request(1L, 1L, 10_000L, PaymentMethod.CARD, null);

        payment.markPending("TX-OK");
        payment.approve();

        assertThat(payment.getPgTxId()).isEqualTo("TX-OK");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
    }

    @Test
    @DisplayName("거절 과정: REQUESTED → PENDING → DECLINED")
    void lifecycle_requested_pending_declined() {
        var payment = PaymentEntity.request(1L, 1L, 10_000L, PaymentMethod.CARD, null);

        payment.markPending("TX-NO");
        payment.decline();

        assertThat(payment.getPgTxId()).isEqualTo("TX-NO");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.DECLINED);
    }
}
