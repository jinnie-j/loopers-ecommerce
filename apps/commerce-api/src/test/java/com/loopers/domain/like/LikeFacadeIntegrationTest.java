package com.loopers.domain.like;

import com.loopers.application.like.LikeFacade;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LikeFacade 통합 테스트")
@SpringBootTest
@Transactional
public class LikeFacadeIntegrationTest {

    @Autowired
    private LikeFacade likeFacade;
    @Autowired private UserService userService;
    @Autowired private BrandService brandService;
    @Autowired private ProductService productService;
    @Autowired private LikeService likeService;

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
}
