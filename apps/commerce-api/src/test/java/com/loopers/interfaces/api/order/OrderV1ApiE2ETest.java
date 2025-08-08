package com.loopers.interfaces.api.order;

import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderInfo;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.point.PointEntity;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Order API E2E 테스트")
public class OrderV1ApiE2ETest {
    /*
     * 주문 생성 E2E 테스트
     * - [x] 존재하는 상품으로 주문을 생성하면, 주문 정보가 반환된다.
     * - [x] 재고보다 많은 수량을 주문할 경우, 400 Bad Request 응답을 반환한다.
     * - [x] 존재하지 않는 상품으로 주문할 경우, 404 Not Found 응답을 반환한다.
     *
     * 주문 목록 조회 E2E 테스트
     * - [x] 유저 ID로 요청 시, 주문 목록이 반환된다.
     *
     * 주문 상세 조회 E2E 테스트
     * - [x] 존재하는 주문 ID 조회 시, 상세 주문 정보가 반환된다.
     * - [x] 존재하지 않는 주문 ID 조회 시, 404 Not Found 응답을 반환한다.
     */
    private final TestRestTemplate testRestTemplate;
    private final DatabaseCleanUp databaseCleanUp;
    private final ProductRepository productRepository;
    private final PointRepository pointRepository;
    private final OrderService orderService;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Autowired
    public OrderV1ApiE2ETest(TestRestTemplate testRestTemplate, ProductRepository productRepository, PointRepository pointRepository, DatabaseCleanUp databaseCleanUp, OrderService orderService) {
        this.testRestTemplate = testRestTemplate;
        this.productRepository = productRepository;
        this.pointRepository = pointRepository;
        this.databaseCleanUp = databaseCleanUp;
        this.orderService = orderService;
    }
    private static final String ENDPOINT = "/api/v1/orders";

    private final Long userId = 1L;

    private ProductEntity createProduct(Long stock) {
        return productRepository.save(ProductEntity.of("상품", 1000L, stock, 1L));
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", userId.toString());
        return headers;
    }

    @Nested
    @DisplayName("주문 생성")
    class CreateOrder {

        @Test
        @DisplayName("존재하는 상품으로 주문을 생성하면, 주문 정보가 반환된다.")
        void getOrders_success() {

            ProductEntity product = createProduct(10L);
            pointRepository.save(new PointEntity(userId, 10000L));

            // 주문 생성
            OrderRequest.Create req = new OrderRequest.Create(
                    List.of(new OrderCommand.OrderItem(product.getId(), 1L, product.getPrice())),
                    null
            );
            HttpHeaders headers = createHeaders();
            HttpEntity<OrderRequest.Create> createEntity = new HttpEntity<>(req, headers);

            ParameterizedTypeReference<ApiResponse<OrderResponse.Detail>> createType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<OrderResponse.Detail>> created = testRestTemplate.exchange(
                    ENDPOINT, HttpMethod.POST, createEntity, createType
            );

            assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(created.getBody()).isNotNull();
            Long createdOrderId = created.getBody().data().orderId();
            assertThat(createdOrderId).isNotNull();

            ParameterizedTypeReference<ApiResponse<List<OrderResponse.Detail>>> listType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<List<OrderResponse.Detail>>> listResp = testRestTemplate.exchange(
                    ENDPOINT, HttpMethod.GET, new HttpEntity<>(headers), listType
            );

            assertThat(listResp.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(listResp.getBody()).isNotNull();
            List<OrderResponse.Detail> list = listResp.getBody().data();
            assertThat(list).isNotEmpty();
            assertThat(list)
                    .anySatisfy(o -> {
                        assertThat(o.orderId()).isEqualTo(createdOrderId);
                        assertThat(o.orderItems()).hasSize(1);
                        assertThat(o.orderItems().get(0).productId()).isEqualTo(product.getId());
                    });
        }

        @Test
        @DisplayName("재고보다 많은 수량을 주문할 경우, 400 Bad Request 응답을 반환한다.")
        void createOrder_exceedsStock() {
            ProductEntity product = createProduct(1L);

            OrderRequest.Create request = new OrderRequest.Create(
                    List.of(new OrderCommand.OrderItem(product.getId(), 10L, product.getPrice())), null
            );
            HttpEntity<OrderRequest.Create> entity = new HttpEntity<>(request, createHeaders());
            ParameterizedTypeReference<ApiResponse<OrderResponse.Detail>> responseType = new ParameterizedTypeReference<>() {};

            // act
            ResponseEntity<ApiResponse<OrderResponse.Detail>> response =
                    testRestTemplate.exchange(ENDPOINT, HttpMethod.POST, entity, responseType);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("존재하지 않는 상품으로 주문할 경우, 404 Not Found 응답을 반환한다.")
        void createOrder_nonexistentProduct() {
            OrderRequest.Create request = new OrderRequest.Create(
                    List.of(new OrderCommand.OrderItem(9999L, 1L, 1000L)), null
            );
            HttpEntity<OrderRequest.Create> entity = new HttpEntity<>(request, createHeaders());

            ResponseEntity<ApiResponse> response = testRestTemplate.postForEntity(ENDPOINT, entity, ApiResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("주문 목록 조회")
    class GetOrders {

        @Test
        @DisplayName("유저 ID로 요청 시, 주문 목록이 반환된다.")
        void getOrders_success() {
            ProductEntity product = createProduct(10L);
            OrderRequest.Create request = new OrderRequest.Create(
                    List.of(new OrderCommand.OrderItem(product.getId(), 1L, product.getPrice())), null
            );
            HttpEntity<OrderRequest.Create> entity = new HttpEntity<>(request, createHeaders());
            testRestTemplate.postForEntity(ENDPOINT, entity, ApiResponse.class);

            ResponseEntity<ApiResponse> response = testRestTemplate.exchange(
                    ENDPOINT, HttpMethod.GET, new HttpEntity<>(createHeaders()), ApiResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().data()).isNotNull();
        }
    }

    @Nested
    @DisplayName("주문 상세 조회")
    class GetOrder {

        @Test
        @DisplayName("존재하는 주문 ID 조회 시, 상세 주문 정보가 반환된다.")
        void getOrder_success() {

            ProductEntity product = createProduct(10L);
            pointRepository.save(new PointEntity(userId, 10000L));
            OrderRequest.Create request = new OrderRequest.Create(
                    List.of(new OrderCommand.OrderItem(product.getId(), 1L, product.getPrice())), null
            );
            HttpEntity<OrderRequest.Create> entity = new HttpEntity<>(request, createHeaders());
            ParameterizedTypeReference<ApiResponse<OrderResponse.Detail>> responseType = new ParameterizedTypeReference<>() {};

            ResponseEntity<ApiResponse<OrderResponse.Detail>> createdResponse = testRestTemplate.exchange(
                    ENDPOINT, HttpMethod.POST, entity, responseType
            );
            Long orderId = createdResponse.getBody().data().orderId();
            //act
            HttpEntity<Void> getEntity = new HttpEntity<>(createHeaders());
            ResponseEntity<ApiResponse<OrderResponse.Detail>> response = testRestTemplate.exchange(
                    ENDPOINT + "/" + orderId, HttpMethod.GET, getEntity, responseType
            );
            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().data().orderId()).isEqualTo(orderId);
            assertThat(response.getBody().data().orderItems()).hasSize(1);
        }

        @Test
        @DisplayName("존재하지 않는 주문 ID 조회 시, 404 Not Found 응답을 반환한다.")
        void getOrder_notFound() {
            ResponseEntity<ApiResponse> response = testRestTemplate.exchange(
                    ENDPOINT + "/99999", HttpMethod.GET, new HttpEntity<>(createHeaders()), ApiResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}
