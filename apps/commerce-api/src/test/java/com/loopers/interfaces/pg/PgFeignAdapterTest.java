package com.loopers.interfaces.pg;

import com.loopers.application.payment.PaymentGateway;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.infrastructure.payment.pg.PgFeignAdapter;
import com.loopers.infrastructure.payment.pg.PgFeignClient;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.net.SocketTimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
        "pg.user-id=135135",
        "pg.base-url=http://localhost:9999",
        "pg.callback-url=http://localhost:8080/api/v1/payments/callback",
        "payments.recon.batch-size=200"
})

public class PgFeignAdapterTest {

    @Autowired
    PgFeignAdapter adapter;
    @MockitoBean
    PgFeignClient client;
    @Autowired
    CircuitBreakerRegistry cbRegistry;

    @BeforeEach
    void setUp() {
        cbRegistry.circuitBreaker("pg").reset();
        Mockito.reset(client);
    }

    private PaymentGateway.CreatePaymentRequest newReq(String orderId) {
        return new PaymentGateway.CreatePaymentRequest(
                orderId,
                5000L,
                "http://localhost:8080/api/v1/payments/callback",
                PaymentMethod.CARD,
                "SAMSUNG",
                "4111-1111-1111-1111"
        );
    }

    @Test
    @DisplayName("타임아웃 2회, 3번째 성공 → Retry 통해 정상 성공")
    void requestPayment_retryThenSuccess() throws Exception {
        when(client.request(any(), anyString()))
                .thenThrow(new RuntimeException(new SocketTimeoutException("t1")))
                .thenThrow(new RuntimeException(new SocketTimeoutException("t2")))
                .thenReturn(new PaymentGateway.CreatePaymentResponse("TX-123", "APPROVED"));

        var res = adapter.requestPayment(newReq("O-1"));

        assertThat(res.transactionId()).isEqualTo("TX-123");
        assertThat(res.status()).isEqualTo("APPROVED");
        verify(client, times(3)).request(any(), eq("135135"));
    }
    @Test
    @DisplayName("타임아웃 3회 모두 실패 → Fallback(RETRY_LATER) 호출")
    void requestPayment_exhaustRetries_callsFallback() throws Exception {
        when(client.request(any(), anyString()))
                .thenThrow(new RuntimeException(new SocketTimeoutException("t1")))
                .thenThrow(new RuntimeException(new SocketTimeoutException("t2")))
                .thenThrow(new RuntimeException(new SocketTimeoutException("t3")));

        var res = adapter.requestPayment(newReq("O-2"));

        assertThat(res.transactionId()).isNull();
        assertThat(res.status()).isEqualTo("RETRY_LATER");
        verify(client, times(3)).request(any(), eq("135135"));
    }

    @Test
    @DisplayName("CircuitBreaker가 OPEN일 때 호출 → Fallback 실행")
    void circuitBreaker_open_shortCircuits_and_callsFallback() {
        CircuitBreaker cb = cbRegistry.circuitBreaker("pg");
        // 테스트에서는 명시적으로 OPEN 전이시켜 단락 동작만 확인
        cb.transitionToOpenState();

        var dto = adapter.getPaymentByTx("TR-OPEN");

        assertThat(dto.transactionId()).isEqualTo("TR-OPEN");
        assertThat(dto.status()).isEqualTo("UNKNOWN"); // 폴백 값
        verify(client, never()).getByTx(anyString(), anyString());
    }

    @Test
    @DisplayName("getPaymentByTx - 타임아웃 발생 시 Fallback(UNKNOWN) 반환")
    void getPaymentByTx_timeout_callsFallback() throws Exception {
        when(client.getByTx(anyString(), anyString()))
                .thenThrow(new RuntimeException(new SocketTimeoutException("timeout")));

        var dto = adapter.getPaymentByTx("TR-1");

        assertThat(dto.transactionId()).isEqualTo("TR-1");
        assertThat(dto.status()).isEqualTo("UNKNOWN");
        verify(client, times(3)).getByTx(eq("TR-1"), eq("135135")); // 3회 시도(설정 값)
    }

    @Test
    @DisplayName("findPaymentsByOrderId - 타임아웃 발생 시 빈 리스트 Fallback")
    void findByOrder_timeout_returnsEmptyListFallback() throws Exception {
        when(client.findByOrder(anyString(), anyString()))
                .thenThrow(new RuntimeException(new SocketTimeoutException("timeout")));

        var list = adapter.findPaymentsByOrderId("O-3");

        assertThat(list).isEmpty();
        verify(client, times(3)).findByOrder(eq("O-3"), eq("135135"));
    }

}
