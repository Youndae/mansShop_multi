package com.example.moduleproduct.repository;

import com.example.moduleauth.repository.MemberRepository;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductQnA;
import com.example.modulecommon.model.entity.ProductQnAReply;
import com.example.moduleproduct.ModuleProductApplication;
import com.example.moduleproduct.fixture.MemberFixture;
import com.example.moduleproduct.fixture.ProductFixture;
import com.example.moduleproduct.fixture.ProductQnAFixture;
import com.example.moduleproduct.model.dto.product.business.ProductQnADTO;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productQnA.ProductQnARepository;
import com.example.moduleproduct.repository.productQnAReply.ProductQnAReplyRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = ModuleProductApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
public class ProductQnAReplyRepositoryTest {

    @Autowired
    private ProductQnAReplyRepository productQnAReplyRepository;

    @Autowired
    private ProductQnARepository productQnARepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * 상품 문의 조회는 여러개의 ProductQnA의 각 ID를 리스트화 해 조회하므로
     * 여러개의 ProductQnA를 받아 save
     * member와 Product는 동일한 데이터를 갖고 있는 Entity이므로 때문에 한번만 save
     */
    @BeforeEach
    void init() {
        ProductQnA productQnA = ProductQnAFixture.createProductQnAAndReply(1);
        ProductQnA productQnA2 = ProductQnAFixture.createProductQnAAndReply(2);
        List<ProductQnA> entityList = List.of(productQnA, productQnA2);

        memberRepository.save(productQnA.getMember());
        productRepository.save(productQnA.getProduct());
        productQnARepository.saveAll(entityList);
    }

    @Test
    @DisplayName("여러개의 productQnAId List를 통한 조회.")
    void findByQnAReplyData() {
        ProductQnA productQnA = ProductQnAFixture.createProductQnAAndReply(1);
        ProductQnA productQnA2 = ProductQnAFixture.createProductQnAAndReply(2);
        List<ProductQnAReply> replyAllList = new ArrayList<>();
        replyAllList.addAll(productQnA.getProductQnAReplies());
        replyAllList.addAll(productQnA2.getProductQnAReplies());

        List<Long> qnaIds = productQnARepository.findByProductId(productQnA.getProduct().getId(),
                                                        ProductQnAFixture.createQnAPageable()
                                                )
                                                .stream()
                                                .mapToLong(ProductQnADTO::qnaId)
                                                .boxed()
                                                .toList();

        List<ProductQnAReply> replyResult = productQnAReplyRepository.findByQnAReply(qnaIds);

        Assertions.assertEquals(replyAllList.size(), replyResult.size());
    }
}
