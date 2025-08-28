package com.loopers.domain.payment;

import com.loopers.application.order.event.OrderPaymentEventHandler;
import com.loopers.application.payment.PaymentGateway;
import com.loopers.domain.payment.event.PaymentApprovedEvent;
import com.loopers.domain.payment.event.PaymentDeclinedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("PaymentService 통합 테스트")
@SpringBootTest(properties = {
        "pg.base-url=http://localhost:9999",
        "pg.user-id=135135",
        "pg.callback-url=http://localhost:8080/api/v1/payments/callback",
        "payments.recon.batch-size=50",
        "payments.recon.fixed-delay=10s"
})
@RecordApplicationEvents
public class PaymentServiceIntegrationTest {

    @Autowired PaymentService paymentService;
    @Autowired PaymentRepository paymentRepository;
    @MockitoBean PaymentGateway gateway;
    @Autowired ApplicationEvents events;
    @MockitoBean OrderPaymentEventHandler orderPaymentEventHandler;

    @Test
    @DisplayName("PG=APPROVED 이면 결제를 APPROVED로 확정하고 이벤트 발행")
    public void approve() {
        // arrange
        PaymentEntity p = PaymentEntity.request(1L, 10L, 3000L, PaymentMethod.CARD, null);
        p.markPending("TX-A");
        paymentRepository.save(p);

        when(gateway.getPaymentByTx("TX-A"))
                .thenReturn(new PaymentGateway.PgPaymentDto("TX-A", null, "APPROVED", null));

        // act
        int processed = paymentService.reconcilePaymentStatuses(
                LocalDateTime.now().plusSeconds(1), 100);

        // assert
        assertThat(processed).isEqualTo(1);
        PaymentEntity reloaded = paymentRepository.findById(p.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(PaymentStatus.APPROVED);

        // 이벤트 발행 검증
        var approvedEvents = events.stream(PaymentApprovedEvent.class).toList();
        assertThat(approvedEvents).hasSize(1);
        assertThat(approvedEvents.get(0).orderId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("PG=DECLINED 이면 DECLINED 확정 및 이벤트 발행")
    public void decline() {
        // arrange
        PaymentEntity p = PaymentEntity.request(2L, 20L, 5000L, PaymentMethod.CARD, null);
        p.markPending("TX-D");
        paymentRepository.save(p);

        when(gateway.getPaymentByTx("TX-D"))
                .thenReturn(new PaymentGateway.PgPaymentDto("TX-D", null, "DECLINED", null));

        // act
        int processed = paymentService.reconcilePaymentStatuses(
                LocalDateTime.now().plusSeconds(1), 100);

        // assert
        assertThat(processed).isEqualTo(1);
        PaymentEntity reloaded = paymentRepository.findById(p.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(PaymentStatus.DECLINED);

        var declinedEvents = events.stream(PaymentDeclinedEvent.class).toList();
        assertThat(declinedEvents).hasSize(1);
        assertThat(declinedEvents.get(0).orderId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("PG=PENDING/UNKNOWN이면 상태 유지(처리 건수 증가 안 함, 이벤트 없음)")
    public void pendingOrUnknown() {
        // arrange
        PaymentEntity p = PaymentEntity.request(3L, 30L, 1000L,PaymentMethod.CARD, null);
        p.markPending("TX-P");
        paymentRepository.save(p);

        when(gateway.getPaymentByTx("TX-P"))
                .thenReturn(new PaymentGateway.PgPaymentDto("TX-P", null, "PENDING", null));

        // act
        int processed = paymentService.reconcilePaymentStatuses(
                LocalDateTime.now().plusSeconds(1), 100);

        // assert
        assertThat(processed).isEqualTo(0);
        PaymentEntity reloaded = paymentRepository.findById(p.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(PaymentStatus.PENDING);

        assertThat(events.stream(PaymentApprovedEvent.class).count()).isZero();
        assertThat(events.stream(PaymentDeclinedEvent.class).count()).isZero();
    }

    @Test
    @DisplayName("이미 APPROVED/DECLINED면 재확정하지 않음")
    public void idempotent() {
        // arrange
        PaymentEntity p = PaymentEntity.request(4L, 40L, 2000L, PaymentMethod.CARD, null);
        p.approve();
        paymentRepository.save(p);

        // act
        int processed = paymentService.reconcilePaymentStatuses(
                LocalDateTime.now().minusSeconds(10), 100);

        // assert
        assertThat(processed).isEqualTo(0);
        assertThat(events.stream(PaymentApprovedEvent.class).count()).isZero();
    }
}
