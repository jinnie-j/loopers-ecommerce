package com.loopers.interfaces.api.coupon;

import com.loopers.domain.coupon.CouponEntity;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.DiscountType;
import com.loopers.domain.userCoupon.UserCouponEntity;
import com.loopers.domain.userCoupon.UserCouponRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CouponV1ApiE2ETest {

    private static final long USER_ID = 1L;
    private static final String ENDPOINT = "/api/v1/coupons";

    private final TestRestTemplate testRestTemplate;
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final DatabaseCleanUp databaseCleanUp;

    @Autowired
    CouponV1ApiE2ETest(TestRestTemplate testRestTemplate, CouponRepository couponRepository, UserCouponRepository userCouponRepository, DatabaseCleanUp databaseCleanUp) {
        this.testRestTemplate = testRestTemplate;
        this.couponRepository = couponRepository;
        this.userCouponRepository = userCouponRepository;
        this.databaseCleanUp = databaseCleanUp;
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private CouponEntity saveFixedAmount(long amount, LocalDateTime expiredAt) {
        CouponEntity coupon = CouponEntity.of("정액쿠폰", DiscountType.FIXED_AMOUNT, amount, null, expiredAt);
        return couponRepository.save(coupon);
    }

    private CouponEntity saveFixedRate(double rate, LocalDateTime expiredAt) {
        CouponEntity coupon = CouponEntity.of("정률쿠폰", DiscountType.FIXED_RATE, null, rate, expiredAt);
        return couponRepository.save(coupon);
    }

    @Nested
    @DisplayName("쿠폰 생성")
    class Create {

        @Test
        @DisplayName("유효한 요청이면 200 OK와 생성된 쿠폰을 반환한다")
        void returnsCoupon_whenCreateIsSuccessful() {
            // arrange
            var couponRequest = new CouponRequest.Create(
                    "신규쿠폰",
                    DiscountType.FIXED_AMOUNT,
                    2_000L,
                    null,
                    LocalDateTime.now().plusDays(1).toString()
            );
            HttpEntity<CouponRequest.Create> entity = new HttpEntity<>(couponRequest, jsonHeaders());
            ParameterizedTypeReference<ApiResponse<CouponResponse>> type = new ParameterizedTypeReference<>() {};

            // act
            ResponseEntity<ApiResponse<CouponResponse>> res =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, entity, type);

            // assert
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(res.getBody().data()).isNotNull();
            assertThat(res.getBody().data().name()).isEqualTo("신규쿠폰");
            assertThat(res.getBody().data().discountType()).isEqualTo(DiscountType.FIXED_AMOUNT.name());
        }
    }

    @Nested
    @DisplayName("쿠폰 조회")
    class GetCoupon {

        @Test
        @DisplayName("사용 가능한 쿠폰이면 200 OK와 쿠폰 정보를 반환한다")
        void returnsCoupon_whenAvailable() {
            // arrange
            var saved = saveFixedAmount(1_000L, LocalDateTime.now().plusDays(1));
            String url = ENDPOINT + "/" + saved.getId();

            HttpEntity<Void> entity = new HttpEntity<>(jsonHeaders());
            ParameterizedTypeReference<ApiResponse<CouponResponse>> type = new ParameterizedTypeReference<>() {};

            // act
            var res = testRestTemplate.exchange(url, HttpMethod.GET, entity, type);

            // assert
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(res.getBody()).isNotNull();
            assertThat(res.getBody().data()).isNotNull();
            assertThat(res.getBody().data().couponId()).isEqualTo(saved.getId());
        }

        @Test
        @DisplayName("존재하지 않는 쿠폰이면 404 Not Found를 반환한다")
        void returnsNotFound_whenCouponDoesNotExist() {
            // arrange
            String url = ENDPOINT + "/999999";
            HttpEntity<Void> entity = new HttpEntity<>(jsonHeaders());
            ParameterizedTypeReference<ApiResponse<CouponResponse>> type = new ParameterizedTypeReference<>() {};

            // act
            var res = testRestTemplate.exchange(url, HttpMethod.GET, entity, type);

            // assert
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(res.getBody()).isNotNull();
            assertThat(res.getBody().data()).isNull();
        }

        @Test
        @DisplayName("만료된 쿠폰이면 400 Bad Request를 반환한다")
        void returnsBadRequest_whenCouponExpired() {
            // arrange
            var expired = saveFixedAmount(1_000L, LocalDateTime.now().minusDays(1));
            String url = ENDPOINT + "/" + expired.getId();

            HttpEntity<Void> entity = new HttpEntity<>(jsonHeaders());
            ParameterizedTypeReference<ApiResponse<CouponResponse>> type = new ParameterizedTypeReference<>() {};

            // act
            var res = testRestTemplate.exchange(url, HttpMethod.GET, entity, type);

            // assert
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(res.getBody()).isNotNull();
            assertThat(res.getBody().data()).isNull();
        }
    }

    @Nested
    @DisplayName("쿠폰 할인 적용")
    class ApplyCoupon {

        @Test
        @DisplayName("정액 쿠폰을 소유하고 있으면 할인된 금액을 반환한다")
        void returnsDiscounted_whenFixedAmountAndOwned() {
            // arrange
            var coupon = saveFixedAmount(2_000L, LocalDateTime.now().plusDays(1));
            userCouponRepository.save(UserCouponEntity.of(USER_ID, coupon.getId()));


            var body = new CouponRequest.Apply(USER_ID, coupon.getId(), 10_000L);
            HttpEntity<CouponRequest.Apply> entity = new HttpEntity<>(body, jsonHeaders());
            ParameterizedTypeReference<ApiResponse<Long>> type = new ParameterizedTypeReference<>() {};

            // act
            var res = testRestTemplate.exchange(ENDPOINT + "/apply", HttpMethod.POST, entity, type);

            // assert
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(res.getBody()).isNotNull();
            assertThat(res.getBody().data()).isEqualTo(8_000L);
        }

        @Test
        @DisplayName("정률 쿠폰을 소유하고 있으면 할인 금액이 계산된다")
        void returnsDiscounted_whenFixedRateAndOwned() {
            // arrange
            var coupon = saveFixedRate(0.10, LocalDateTime.now().plusDays(1)); // 10%
            userCouponRepository.save(UserCouponEntity.of(USER_ID, coupon.getId()));

            var body = new CouponRequest.Apply(USER_ID, coupon.getId(), 10_005L);
            HttpEntity<CouponRequest.Apply> entity = new HttpEntity<>(body, jsonHeaders());
            ParameterizedTypeReference<ApiResponse<Long>> type = new ParameterizedTypeReference<>() {};

            // act
            var res = testRestTemplate.exchange(ENDPOINT + "/apply", HttpMethod.POST, entity, type);

            // assert
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(res.getBody()).isNotNull();
            assertThat(res.getBody().data()).isEqualTo((long) (10_005L * (1 - 0.10))); // 9004
        }

        @Test
        @DisplayName("쿠폰을 소유하지 않으면 404 Not Found를 반환한다")
        void returnsNotFound_whenCouponNotOwned() {
            // arrange
            var coupon = saveFixedAmount(1_000L, LocalDateTime.now().plusDays(1));

            var body = new CouponRequest.Apply(USER_ID, coupon.getId(), 5_000L);
            HttpEntity<CouponRequest.Apply> entity = new HttpEntity<>(body, jsonHeaders());
            ParameterizedTypeReference<ApiResponse<Long>> type = new ParameterizedTypeReference<>() {};

            // act
            var res = testRestTemplate.exchange(ENDPOINT + "/apply", HttpMethod.POST, entity, type);

            // assert
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(res.getBody()).isNotNull();
            assertThat(res.getBody().data()).isNull();
        }
    }
}
