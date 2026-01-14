package com.example.moduletest.productQnA.usecase;

import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.customException.CustomNotFoundException;
import com.example.modulecommon.fixture.ClassificationFixture;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.fixture.ProductQnAFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.dto.page.MyPagePageDTO;
import com.example.modulecommon.model.dto.qna.out.QnADetailReplyDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.utils.TestPaginationUtils;
import com.example.moduleproduct.model.dto.productQnA.out.ProductQnADetailResponseDTO;
import com.example.moduleproduct.model.dto.productQnA.out.ProductQnAListDTO;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productQnA.ProductQnARepository;
import com.example.moduleproduct.repository.productQnAReply.ProductQnAReplyRepository;
import com.example.moduleproduct.usecase.productQnA.ProductQnAReadUseCase;
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
import org.springframework.data.domain.Page;
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
public class ProductQnAReadUseCaseIT {

    @Autowired
    private ProductQnAReadUseCase productQnAReadUseCase;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ClassificationRepository classificationRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductQnARepository productQnARepository;

    @Autowired
    private ProductQnAReplyRepository productQnAReplyRepository;

    @Autowired
    private EntityManager em;

    private Member member;

    private List<Member> memberList;

    private List<Product> productList;

    private List<ProductQnA> newProductQnAList;

    private List<ProductQnA> completedAnswerProductQnAList;

    private List<ProductQnA> saveProductQnAList;

    private List<ProductQnAReply> productQnAReplies;

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

        productList = ProductFixture.createProductFixtureList(10, classificationList.get(0));
        productRepository.saveAll(productList);

        newProductQnAList = ProductQnAFixture.createDefaultProductQnA(memberList, productList);
        completedAnswerProductQnAList = ProductQnAFixture.createProductQnACompletedAnswer(memberList, productList);
        productQnAReplies = ProductQnAFixture.createDefaultProductQnaReply(admin, completedAnswerProductQnAList);
        saveProductQnAList = new ArrayList<>(newProductQnAList);
        saveProductQnAList.addAll(completedAnswerProductQnAList);
        productQnARepository.saveAll(saveProductQnAList);
        productQnAReplyRepository.saveAll(productQnAReplies);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName(value = "사용자 아이디 기반 상품 문의 목록 조회")
    void getProductQnAList() {
        MyPagePageDTO pageDTO = new MyPagePageDTO(1);
        List<ProductQnA> fixtureList = saveProductQnAList.stream()
                .filter(v -> v.getMember().getUserId().equals(member.getUserId()))
                .toList();
        int contentSize = Math.min(fixtureList.size(), pageDTO.amount());
        int totalPages = TestPaginationUtils.getTotalPages(fixtureList.size(), pageDTO.amount());

        Page<ProductQnAListDTO> result = assertDoesNotThrow(
                () -> productQnAReadUseCase.getProductQnAList(pageDTO, member.getUserId())
        );

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(contentSize, result.getContent().size());
        assertEquals(fixtureList.size(), result.getTotalElements());
        assertEquals(totalPages, result.getTotalPages());
    }

    @Test
    @DisplayName(value = "사용자 아이디 기반 상품 문의 목록 조회. 데이터가 없는 경우")
    void getProductQnAListEmpty() {
        productQnAReplyRepository.deleteAll();
        productQnARepository.deleteAll();
        MyPagePageDTO pageDTO = new MyPagePageDTO(1);

        Page<ProductQnAListDTO> result = assertDoesNotThrow(
                () -> productQnAReadUseCase.getProductQnAList(pageDTO, member.getUserId())
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getContent().size());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
    }

    @Test
    @DisplayName(value = "상품 문의 상세 조회")
    void getProductQnADetail() {
        ProductQnA fixture = completedAnswerProductQnAList.get(0);
        ProductQnAReply replyFixture = productQnAReplies.stream()
                .filter(v -> v.getProductQnA().getId().equals(fixture.getId()))
                .findFirst()
                .get();
        String nickname = fixture.getMember().getNickname();

        ProductQnADetailResponseDTO result = assertDoesNotThrow(
                () -> productQnAReadUseCase.getProductQnADetail(fixture.getId(), nickname)
        );

        assertNotNull(result);
        assertEquals(fixture.getId(), result.productQnAId());
        assertEquals(fixture.getProduct().getProductName(), result.productName());
        assertEquals(nickname, result.writer());
        assertEquals(fixture.getQnaContent(), result.qnaContent());
        assertEquals(fixture.isProductQnAStat(), result.productQnAStat());
        assertEquals(1, result.replyList().size());

        QnADetailReplyDTO resultReply = result.replyList().get(0);

        assertEquals(replyFixture.getId(), resultReply.replyId());
        assertEquals(replyFixture.getMember().getNickname(), resultReply.writer());
        assertEquals(replyFixture.getReplyContent(), resultReply.replyContent());
    }

    @Test
    @DisplayName(value = "상품 문의 상세 조회. 작성자가 일치하지 않는 경우")
    void getProductQnADetailWriterNotEquals() {
        ProductQnA fixture = completedAnswerProductQnAList.get(0);

        assertThrows(
                CustomAccessDeniedException.class,
                () -> productQnAReadUseCase.getProductQnADetail(fixture.getId(), "wrongUserNickname")
        );
    }

    @Test
    @DisplayName(value = "상품 문의 상세 조회. 상품 문의 아이디가 잘못된 경우")
    void getProductQnADetailWrongQnAId() {
        assertThrows(
                CustomNotFoundException.class,
                () -> productQnAReadUseCase.getProductQnADetail(0L, "wrongUserNickname")
        );
    }
}
