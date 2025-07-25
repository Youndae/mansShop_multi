package com.example.moduleproduct.usecase.product.integration;

import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.fixture.*;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleproduct.ModuleProductApplication;
import com.example.moduleproduct.model.dto.product.in.ProductQnAPostDTO;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productLike.ProductLikeRepository;
import com.example.moduleproduct.repository.productQnA.ProductQnARepository;
import com.example.moduleproduct.usecase.product.ProductWriteUseCase;
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

@SpringBootTest(classes = ModuleProductApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@Transactional
public class ProductWriteUseCaseIT {

    @Autowired
    private ProductWriteUseCase productWriteUseCase;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ClassificationRepository classificationRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductLikeRepository productLikeRepository;

    @Autowired
    private ProductQnARepository productQnARepository;

    @Autowired
    private EntityManager em;

    private Member member;

    private List<Member> memberList;

    private Product product;

    private ProductLike productLike;

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

        productLike = ProductLikeFixture.createDefaultProductLike(List.of(member), List.of(product)).get(0);
        productLikeRepository.save(productLike);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName(value = "관심상품 등록")
    void likeProduct() {
        String userId = memberList.get(1).getUserId();
        String result = assertDoesNotThrow(() -> productWriteUseCase.likeProduct(product.getId(), userId));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result);

        ProductLike saveProductLike = productLikeRepository.findByMember_UserId(userId).get(0);

        assertNotNull(saveProductLike);
    }

    @Test
    @DisplayName(value = "관심상품 등록. Principal이 null인 경우")
    void likeProductPrincipalIsNull() {
        assertThrows(
                CustomAccessDeniedException.class,
                () -> productWriteUseCase.likeProduct(product.getId(), null)
        );
    }

    @Test
    @DisplayName(value = "관심상품 등록. 상품 아이디가 잘못 된 경우")
    void likeProductWrongProductId() {
        String userId = memberList.get(1).getUserId();
        assertThrows(
                IllegalArgumentException.class,
                () -> productWriteUseCase.likeProduct("noneProductId", userId)
        );

        List<ProductLike> productLikeList = productLikeRepository.findByMember_UserId(userId);

        assertTrue(productLikeList.isEmpty());
    }

    @Test
    @DisplayName(value = "관심상품 해제")
    void deLikeProduct() {
        String productId = productLike.getProduct().getId();

        String result = assertDoesNotThrow(() -> productWriteUseCase.deleteProductLike(productId, member.getUserId()));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result);

        List<ProductLike> productLikeList = productLikeRepository.findByMember_UserId(member.getUserId());

        assertTrue(productLikeList.isEmpty());
    }

    @Test
    @DisplayName(value = "관심상품 해제. Principal이 null인 경우")
    void deLikeProductPrincipalIsNull() {
        String productId = productLike.getProduct().getId();

        assertThrows(
                CustomAccessDeniedException.class,
                () -> productWriteUseCase.deleteProductLike(productId, null)
        );

        List<ProductLike> productLikeList = productLikeRepository.findByMember_UserId(member.getUserId());

        assertFalse(productLikeList.isEmpty());
    }

    @Test
    @DisplayName(value = "관심상품 해제. 상품 아이디가 잘못 된 경우")
    void deLikeProductWrongId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> productWriteUseCase.deleteProductLike("noneProductId", member.getUserId())
        );

        List<ProductLike> productLikeList = productLikeRepository.findByMember_UserId(member.getUserId());

        assertFalse(productLikeList.isEmpty());
    }

    @Test
    @DisplayName(value = "상품 문의 작성")
    void postProductQnA() {
        ProductQnAPostDTO postDTO = new ProductQnAPostDTO(product.getId(), "test post product QnA content");

        String result = assertDoesNotThrow(() -> productWriteUseCase.postProductQnA(postDTO, member.getUserId()));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result);

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
                () -> productWriteUseCase.postProductQnA(postDTO, member.getUserId())
        );
    }
}
