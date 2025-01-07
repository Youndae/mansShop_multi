package com.example.moduleproduct.repository;

import com.example.moduleauth.repository.MemberRepository;
import com.example.modulecommon.model.entity.ProductQnA;
import com.example.moduleproduct.ModuleProductApplication;
import com.example.moduleproduct.fixture.ProductFixture;
import com.example.moduleproduct.fixture.ProductQnAFixture;
import com.example.moduleproduct.model.dto.product.business.ProductQnADTO;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productQnA.ProductQnARepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@SpringBootTest(classes = ModuleProductApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
public class ProductQnARepositoryTest {

    @Autowired
    private ProductQnARepository productQnARepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ClassificationRepository classificationRepository;

    /**
     * 여러개의 ProductQnA 조회를 위해 2개의 ProductQnA를 생성 후 save
     * Member, Product Entity는 2개 모두 동일한 데이터로 담기 때문에 한번만 save
     */
    @BeforeEach
    void init() {
        ProductQnA productQnA = ProductQnAFixture.createProductQnA(1);
        ProductQnA productQnA2 = ProductQnAFixture.createProductQnA(2);
        List<ProductQnA> entityList = List.of(productQnA, productQnA2);

        classificationRepository.save(productQnA.getProduct().getClassification());
        memberRepository.save(productQnA.getMember());
        productRepository.save(productQnA.getProduct());
        productQnARepository.saveAll(entityList);
    }

    @Test
    @DisplayName("상품 아이디를 통해 상품 문의 리스트 조회. 페이징 포함")
    void findProductQnAByProductId() {
        List<ProductQnA> entityList = List.of(
                                            ProductQnAFixture.createProductQnA(1),
                                            ProductQnAFixture.createProductQnA(2)
                                    );
        Pageable pageable = ProductQnAFixture.createQnAPageable();

        Page<ProductQnADTO> result = productQnARepository.findByProductId(
                                            entityList.get(0).getProduct().getId(),
                                            pageable
                                    );

        Assertions.assertEquals(entityList.size(), result.getTotalElements());
        Assertions.assertEquals(entityList.get(0).getQnaContent(), result.getContent().get(1).qnaContent());
        Assertions.assertEquals(entityList.get(1).getQnaContent(), result.getContent().get(0).qnaContent());
    }
}
