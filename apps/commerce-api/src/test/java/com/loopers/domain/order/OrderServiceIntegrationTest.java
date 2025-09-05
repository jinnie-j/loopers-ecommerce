package com.loopers.domain.order;

import com.loopers.domain.brand.BrandInfo;
import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.user.UserInfo;
import com.loopers.domain.brand.BrandCommand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserService;
import com.loopers.domain.user.vo.Birth;
import com.loopers.domain.user.vo.Email;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = {
        "pg.base-url=http://localhost:9999",
        "pg.user-id=135135",
        "pg.callback-url=http://localhost:8080/api/v1/payments/callback",
        "payments.recon.batch-size=50",
        "payments.recon.fixed-delay=10s"
})
@DisplayName("OrderService 통합 테스트")
public class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private ProductService productService;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }


    private UserInfo createUser() {
        var command = new UserCommand.SignUp("jinnie", "지은", Gender.FEMALE, Birth.of("1997-01-27"), Email.of("jinnie@naver.com")
        );
        return userService.signUp(command);
    }

    private BrandInfo createBrand() {
        var command = new BrandCommand.Create("Nike", "스포츠 브랜드");
        return brandService.create(command);
    }

    private ProductInfo createProduct(String name, Long price, Long stock) {
        var brand = createBrand();
        var command = new ProductCommand.Create(name, brand.id(), price, stock);
        return productService.create(command);
    }

    @DisplayName("주문 등록")
    @Nested
    class Order {
        @DisplayName("유저가 출시된 상품들로 주문시, 성공적으로 주문이 등록된다")
        @Test
        void returnsOrderInfo_whenUserExistsAndProductsAreValid() {
            // arrange
            var userInfo = createUser();

            var product1 = createProduct("신발", 10000L, 10L);
            var product2 = createProduct("티셔츠", 20000L, 5L);

            var orderItems = List.of(
                    new OrderCommand.OrderItem(product1.id(), 2L, product1.price()),
                    new OrderCommand.OrderItem(product2.id(), 1L, product2.price())
            );

            var orderCommand = new OrderCommand.Order(userInfo.id(), orderItems, null);

            // act
            OrderInfo orderInfo = orderService.createOrder(orderCommand);

            // assert
            assertNotNull(orderInfo);
            assertEquals(userInfo.id(), orderInfo.userId());
            assertEquals(2, orderInfo.orderItems().size());
            assertEquals(40000L, orderInfo.totalPrice().longValue());
        }
    }

    @DisplayName("주문 목록 조회")
    @Nested
    class getOrderList {
        @Test
        @DisplayName("유저가 주문한 내역들을 모두 조회할 수 있다.")
        void returnsAllOrders_forGivenUser() {
            // arrange
            var userInfo = createUser();
            var product1 = createProduct("신발", 10000L, 10L);
            var product2 = createProduct("티셔츠", 20000L, 5L);

            var orderItems1 = List.of(new OrderCommand.OrderItem(product1.id(), 2L, product1.price()));
            var orderItems2 = List.of(new OrderCommand.OrderItem(product2.id(), 1L, product2.price()));

            orderService.createOrder(new OrderCommand.Order(userInfo.id(), orderItems1, null));
            orderService.createOrder(new OrderCommand.Order(userInfo.id(), orderItems2, null));

            // act
            List<OrderInfo> orderInfos = orderService.getOrders(userInfo.id());

            // assert
            assertThat(orderInfos).hasSize(2);
            assertThat(orderInfos.get(0).userId()).isEqualTo(userInfo.id());
            assertThat(orderInfos.get(1).userId()).isEqualTo(userInfo.id());
        }
    }

    @DisplayName("주문 상세 조회")
    @Nested
    class getOrderDetails {
        @Test
        @DisplayName("주문 ID를 통해 주문 상세 정보를 조회할 수 있다")
        void returnsOrderDetail_whenOrderIdExists() {
            // arrange
            var userInfo = createUser();

            var product1 = createProduct("신발", 10000L, 10L);
            var product2 = createProduct("티셔츠", 20000L, 5L);

            var orderItems = List.of(
                    new OrderCommand.OrderItem(product1.id(), 2L, product1.price()),
                    new OrderCommand.OrderItem(product2.id(), 1L, product2.price())
            );
            var orderCommand = new OrderCommand.Order(userInfo.id(), orderItems, null);
            var createdOrder = orderService.createOrder(orderCommand);

            // act
            var orderInfo = orderService.getOrder(createdOrder.id());

            // assert
            assertEquals(createdOrder.id(), orderInfo.id());
            assertEquals(userInfo.id(), orderInfo.userId());
            assertEquals(40000L, orderInfo.totalPrice().longValue());
            assertEquals(2, orderInfo.orderItems().size());
        }

        @Test
        @DisplayName("존재하지 않는 주문 ID로 조회할 경우 예외가 발생한다")
        void throwsException_whenOrderIdNotFound() {
            //arrange
            Long invalidOrderId = 999L;

            // act
            CoreException exception = assertThrows(CoreException.class, () -> {
                orderService.getOrder(invalidOrderId);
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);

        }

    }
}

