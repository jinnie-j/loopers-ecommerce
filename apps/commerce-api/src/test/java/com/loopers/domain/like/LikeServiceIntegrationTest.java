package com.loopers.domain.like;

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
import com.loopers.utils.DatabaseCleanUp;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.Assert.*;

@SpringBootTest
@Transactional
@DisplayName("LikeService 통합 테스트")
public class LikeServiceIntegrationTest {

    @Autowired
    private DatabaseCleanUp databaseCleanUp;
    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Autowired
    private LikeService likeService;
    @Autowired
    private UserService userService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private ProductService productService;

    private UserInfo createUser() {
        var command = new UserCommand.SignUp("jinnie", "지은", Gender.FEMALE, Birth.of("1997-01-27"), Email.of("jinnie@naver.com")
        );
        return userService.signUp(command);
    }

    private BrandInfo createBrand() {
        var command = new BrandCommand.Create("Nike", "스포츠 브랜드");
        return brandService.create(command);
    }

    private ProductInfo createProduct(Long price, Long stock) {
        var brand = createBrand();
        var command = new ProductCommand.Create("운동화", brand.id(), price, stock);
        return productService.create(command);
    }

    @DisplayName("회원이 존재하는 상품에 좋아요를 등록할 때, 좋아요를 등록한다.")
    @Test
    void registerLike_whenNotLiked(){
        // arrange
        var userInfo = createUser();
        var productInfo = createProduct(10000L, 10L);
        var likeCommand = new LikeCommand.Create(userInfo.id(), productInfo.id());

        //act
        LikeInfo likeInfo = likeService.like(likeCommand);

        // assert
        assertNotNull(likeInfo);
        assertEquals(userInfo.id(), likeInfo.userId());
        assertEquals(productInfo.id(), likeInfo.productId());
    }

    @DisplayName("회원이 이미 좋아요를 등록한 상품이면, 좋아요를 취소한다.")
    @Test
    void deleteLike_whenAlreadyLiked() {
        // arrange
        var userInfo = createUser();
        var productInfo = createProduct(10000L, 10L);
        var likeCommand = new LikeCommand.Create(userInfo.id(), productInfo.id());

        likeService.like(likeCommand);

        // act
        LikeInfo likeInfo = likeService.unlike(likeCommand);

        // assert
        assertNotNull(likeInfo);
        assertEquals(userInfo.id(), likeInfo.userId());
        assertEquals(productInfo.id(), likeInfo.productId());
        assertFalse(likeInfo.isLike());
    }

    @DisplayName("이미 좋아요한 상품에 대해 다시 좋아요를 등록해도 여전히 좋아요 상태를 유지한다.")
    @Test
    void keepLikeState_whenAlreadyLiked() {
        // arrange
        var userInfo = createUser();
        var productInfo = createProduct(10000L, 10L);
        var likeCommand = new LikeCommand.Create(userInfo.id(), productInfo.id());

        likeService.like(likeCommand);

        // act
        LikeInfo likeInfo = likeService.like(likeCommand);

        // assert
        assertNotNull(likeInfo);
        assertEquals(userInfo.id(), likeInfo.userId());
        assertEquals(productInfo.id(), likeInfo.productId());
        assertTrue(likeInfo.isLike());
    }

    @DisplayName("좋아요를하지 않은 상품에 대해 좋아요 취소 요청을해도 여전히 좋아요 안 한 상태를 유지한다.")
    @Test
    void keepUnlikeState_whenNotLikedYet() {
        // arrange
        var userInfo = createUser();
        var productInfo = createProduct(10000L, 10L);
        var likeCommand = new LikeCommand.Create(userInfo.id(), productInfo.id());

        // act
        LikeInfo likeInfo = likeService.unlike(likeCommand);

        // assert
        assertNotNull(likeInfo);
        assertEquals(userInfo.id(), likeInfo.userId());
        assertEquals(productInfo.id(), likeInfo.productId());
        assertFalse(likeInfo.isLike());
    }
}
