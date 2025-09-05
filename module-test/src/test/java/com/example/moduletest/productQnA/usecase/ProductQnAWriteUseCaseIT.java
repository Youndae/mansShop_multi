package com.example.moduletest.productQnA.usecase;

import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.fixture.ClassificationFixture;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.fixture.ProductQnAFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.Classification;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.model.entity.ProductQnA;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleproduct.model.dto.product.in.ProductQnAPostDTO;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productQnA.ProductQnARepository;
import com.example.moduleproduct.usecase.productQnA.ProductQnAWriteUseCase;
import com.example.moduletest.ModuleTestApplication;
import com.example.moduleuser.repository.MemberRepository;
import jakarta.persistence.EntityManager;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ModuleTestApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@Transactional
public class ProductQnAWriteUseCaseIT {

    @Autowired
    private ProductQnAWriteUseCase productQnAWriteUseCase;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ClassificationRepository classificationRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductQnARepository productQnARepository;

    @Autowired
    private EntityManager em;

    private Member member;

    private List<Member> memberList;

    private Product product;

    private ProductQnA productQnA;

    @BeforeEach
    void init() {
        MemberAndAuthFixtureDTO memberAndAuthFixture = MemberAndAuthFixture.createDefaultMember(10);
        memberList = memberAndAuthFixture.memberList();
        MemberAndAuthFixtureDTO adminFixture = MemberAndAuthFixture.createAdmin();
        Member admin = adminFixture.memberList().get(0);
        List<Member> saveMemberList = new ArrayList<>(memberList);
        saveMemberList.add(admin);
        memberRepository.saveAll(saveMemberList);
        member = memberList.get(0);

        List<Classification> classificationList = ClassificationFixture.createClassifications();
        classificationRepository.saveAll(classificationList);

        product = ProductFixture.createProductFixtureList(1, classificationList.get(0)).get(0);
        productRepository.save(product);

        productQnA = ProductQnAFixture.createDefaultProductQnA(List.of(memberList.get(0)), List.of(product)).get(0);
        productQnARepository.save(productQnA);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName(value = "상품 문의 작성")
    void postProductQnA() {
        ProductQnAPostDTO postDTO = new ProductQnAPostDTO(product.getId(), "test post product QnA content");

        assertDoesNotThrow(() -> productQnAWriteUseCase.postProductQnA(postDTO, member.getUserId()));

        ProductQnA saveList = productQnARepository.findAllByMember_UserIdOrderByIdDesc(member.getUserId()).get(0);

        assertNotNull(saveList);
        assertEquals(postDTO.content(), saveList.getQnaContent());
    }

    @Test
    @DisplayName(value = "상품 문의 작성. 상품 아이디가 잘못 된 경우")
    void postProductQnAWrongProductId() {
        ProductQnAPostDTO postDTO = new ProductQnAPostDTO("noneProductId", "test post product QnA content");

        assertThrows(
                IllegalArgumentException.class,
                () -> productQnAWriteUseCase.postProductQnA(postDTO, member.getUserId())
        );
    }

    @Test
    @DisplayName(value = "상품 문의 제거")
    void deleteProductQnA() {
        assertDoesNotThrow(() -> productQnAWriteUseCase.deleteProductQnA(productQnA.getId(), productQnA.getMember().getUserId()));

        ProductQnA checkDeleteEntity = productQnARepository.findById(productQnA.getId()).orElse(null);

        assertNull(checkDeleteEntity);
    }

    @Test
    @DisplayName(value = "상품 문의 제거. 작성자가 일치하지 않는 경우")
    void deleteProductQnAWriterNotEquals() {
        assertThrows(
                CustomAccessDeniedException.class,
                () -> productQnAWriteUseCase.deleteProductQnA(productQnA.getId(), "noneUser")
        );
    }
}
