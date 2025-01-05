package com.example.moduleproduct.repository;

import com.example.moduleauth.repository.MemberRepository;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductOption;
import com.example.modulecommon.model.entity.ProductReview;
import com.example.moduleproduct.ModuleProductApplication;
import com.example.moduleproduct.fixture.ProductReviewFixture;
import com.example.moduleproduct.model.dto.product.business.ProductReviewResponseDTO;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import com.example.moduleproduct.repository.productReview.ProductReviewRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.List;

@SpringBootTest(classes = ModuleProductApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
public class ProductReviewRepositoryTest {

    @Autowired
    private ProductReviewRepository productReviewRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductOptionRepository productOptionRepository;

    /**
     * ProductReview 조회를 위해 해당 Entity 리스트 생성 및 save
     * Member와 Product, ProductOption은 동일한 데이터를 갖는 Entity들이기 떄문에 한번만 save
     */
    @BeforeEach
    void init() {
        List<ProductReview> reviewEntities = ProductReviewFixture.createProductReviewList();
        Product product = reviewEntities.get(0).getProduct();
        Member member = reviewEntities.get(0).getMember();
        ProductOption productOption = reviewEntities.get(0).getProductOption();

        memberRepository.save(member);
        productRepository.save(product);
        productOptionRepository.save(productOption);
        productReviewRepository.saveAll(reviewEntities);
    }

    @Test
    void findProductReviewByProductId() {
        List<ProductReview> reviewEntities = ProductReviewFixture.createProductReviewList();
        Pageable pageable = ProductReviewFixture.createReviewPageable();
        Page<ProductReviewResponseDTO> result = productReviewRepository.findByProductId(reviewEntities.get(0).getProduct().getId(), pageable);

        Assertions.assertEquals(reviewEntities.size(), result.getTotalElements());

        for(int i = 0; i < reviewEntities.size(); i++) {
            ProductReview entity = reviewEntities.get(reviewEntities.size() - i - 1);
            ProductReviewResponseDTO resultDTO = result.getContent().get(i);

            Assertions.assertEquals(entity.getReviewContent(), resultDTO.reviewContent());
        }

    }
}
