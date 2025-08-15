package com.loopers.application.like;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.like.LikeCommand;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.*;
import com.loopers.domain.user.UserInfo;
import com.loopers.domain.brand.BrandCommand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserService;
import com.loopers.domain.user.vo.Birth;
import com.loopers.domain.user.vo.Email;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("LikeFacade 통합 테스트")
@SpringBootTest
public class LikeFacadeIntegrationTest {

    @Autowired private LikeFacade likeFacade;
    @Autowired private UserService userService;
    @Autowired private BrandService brandService;
    @Autowired private ProductService productService;
    @Autowired private LikeService likeService;
    @Autowired private BrandRepository brandRepository;
    @Autowired private ProductRepository productRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;
    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    private UserInfo userInfo;
    private ProductInfo productInfo;

    @BeforeEach
    void setUp() {
        userInfo = userService.signUp(
                new UserCommand.SignUp(
                        "jinnie",
                        "지은",
                        Gender.FEMALE,
                        Birth.of("1997-01-27"),
                        Email.of("jinnie@naver.com")
                )
        );

        var brand = brandService.create(new BrandCommand.Create("Nike", "스포츠 브랜드"));
        productInfo = productService.create(new ProductCommand.Create("운동화", brand.id(), 99000L, 20L));
    }

    @Test
    @DisplayName("회원이 상품을 좋아요 한 후, 좋아요한 상품 목록을 조회할 수 있다.")
    public void getLikedProducts() {
        // arrange
        likeService.like(new LikeCommand.Create(userInfo.id(), productInfo.id()));

        // act
        List<ProductInfo> likedProducts = likeFacade.getLikedProducts(userInfo.id());

        // assert
        assertThat(likedProducts).hasSize(1);
        ProductInfo likedProduct = likedProducts.getFirst();
        assertThat(likedProduct.id()).isEqualTo(productInfo.id());
        assertThat(likedProduct.name()).isEqualTo(productInfo.name());
        assertThat(likedProduct.price()).isEqualTo(productInfo.price());
        assertThat(likedProduct.stock()).isEqualTo(productInfo.stock());
    }

    @Test
    @DisplayName("같은 유저가 동일 상품에 대해 동시에 여러 번 '좋아요' 요청해도 최종 좋아요 수는 1")
    void concurrent_like_sameUser_isIdempotent() throws Exception {
        final long userId = 1L;
        final int threads = 50;
        BrandEntity brand = brandRepository.save(BrandEntity.of("Brand", "desc"));
        ProductEntity product = productRepository.save(ProductEntity.of("상품", 10_000L, 100L, brand.getId()));

        ExecutorService es = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            es.submit(() -> {
                try {
                    start.await();
                    likeService.like(new com.loopers.domain.like.LikeCommand.Create(userId, product.getId()));
                } catch (Exception ignored) {
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        es.shutdown();
        assertTrue(es.awaitTermination(10, TimeUnit.SECONDS));
        long count = likeService.countByProductId(product.getId());
        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("여러 유저가 동시에 같은 상품에 좋아요를 요청해도 중복 없이 유저 수 만큼만 증가")
    void concurrent_like_manyUsers_distinctOnly() throws Exception {
        final int users = 100; // 100명의 서로 다른 유저

        BrandEntity brand = brandRepository.save(BrandEntity.of("Brand", "desc"));
        ProductEntity product = productRepository.save(ProductEntity.of("상품", 10_000L, 100L, brand.getId()));


        ExecutorService es = Executors.newFixedThreadPool(32);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(users);

        for (int i = 0; i < users; i++) {
            final long userId = i + 1L;
            es.submit(() -> {
                try {
                    start.await();
                    likeService.like(new com.loopers.domain.like.LikeCommand.Create(userId, product.getId()));
                } catch (Exception ignored) {
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        es.shutdown();
        assertTrue(es.awaitTermination(10, TimeUnit.SECONDS));
        long count = likeService.countByProductId(product.getId());
        assertThat(count).isEqualTo(users);
    }

}
