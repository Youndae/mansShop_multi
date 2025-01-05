package com.example.moduleproduct.repository;

import com.example.moduleauth.repository.MemberRepository;
import com.example.modulecommon.model.entity.Auth;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductLike;
import com.example.modulecommon.model.enumuration.OAuthProvider;
import com.example.moduleproduct.ModuleProductApplication;
import com.example.moduleproduct.fixture.ProductFixture;
import com.example.moduleproduct.fixture.ProductLikeFixture;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productLike.ProductLikeRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = ModuleProductApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = {"com.example.moduleproduct.repository", "com.example.moduleauth.repository"})
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
public class ProductLikeRepositoryTest {

    @Autowired
    private ProductLikeRepository productLikeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberRepository memberRepository;

    /**
     * 관심상품 등록 여부 조회를 위해 ProductLike Entity 생성.
     * Member, Product Entity에 대한 데이터가 필요하기 때문에 save후 ProductLike save
     */
    @BeforeEach
    void init() {
        ProductLike productLike = ProductLikeFixture.createSuccessLikeCountEntity();
        memberRepository.save(productLike.getMember());
        productRepository.save(productLike.getProduct());
        productLikeRepository.save(productLike);
    }


    @Test
    @DisplayName("사용자가 해당 상품을 관심상품으로 등록한 경우.")
    void userLikeToProduct() {
        ProductLike entity = ProductLikeFixture.createSuccessLikeCountEntity();

        int result = productLikeRepository.countByUserIdAndProductId(entity.getProduct().getId(), entity.getMember().getUserId());

        Assertions.assertEquals(1, result);
    }

    @Test
    @DisplayName("사용자가 해당 상품을 관심상품으로 등록하지 않은 경우.")
    void userLikeToProductFail() {
        ProductLike entity = ProductLikeFixture.createFailLikeCountEntity();

        int result = productLikeRepository.countByUserIdAndProductId(entity.getProduct().getId(), entity.getMember().getUserId());

        Assertions.assertEquals(0, result);
    }

    @Test
    @DisplayName("사용자의 관심상품 해제 후 조회 시 0이 반환")
    void userDeLike() {
        ProductLike entity = ProductLikeFixture.createSuccessLikeCountEntity();

        productLikeRepository.deleteByUserIdAndProductId(entity);

        int result = productLikeRepository.countByUserIdAndProductId(entity.getProduct().getId(), entity.getMember().getUserId());

        Assertions.assertEquals(0, result);
    }



}
