package com.example.moduletest.admin.productQnA.usecase;

import com.example.modulecommon.fixture.*;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.dto.qna.in.QnAReplyInsertDTO;
import com.example.modulecommon.model.dto.qna.in.QnAReplyPatchDTO;
import com.example.modulecommon.model.dto.qna.out.QnADetailReplyDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.NotificationType;
import com.example.modulecommon.model.enumuration.Result;
import com.example.modulenotification.repository.NotificationRepository;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productQnA.ProductQnARepository;
import com.example.moduleproduct.repository.productQnAReply.ProductQnAReplyRepository;
import com.example.moduleproduct.service.productQnA.ProductQnAExternalService;
import com.example.moduleproduct.usecase.admin.productQnA.AdminProductQnAWriteUseCase;
import com.example.moduletest.ModuleTestApplication;
import com.example.moduleuser.repository.AuthRepository;
import com.example.moduleuser.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = ModuleTestApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
public class AdminProductQnAWriteUseCaseIT {

    @Autowired
    private AdminProductQnAWriteUseCase adminProductQnAWriteUseCase;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ClassificationRepository classificationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private ProductQnARepository productQnARepository;

    @Autowired
    private ProductQnAReplyRepository productQnAReplyRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @MockitoBean
    private ProductQnAExternalService productQnAExternalService;

    private List<Product> productList;

    private List<Member> memberList;

    private Member admin;

    private List<ProductQnA> newProductQnAList;

    private List<ProductQnA> allProductQnA;

    private List<ProductQnAReply> productQnAReplyList;

    @BeforeEach
    void init() {
        MemberAndAuthFixtureDTO memberAndAuthFixture = MemberAndAuthFixture.createDefaultMember(30);
        memberList = memberAndAuthFixture.memberList();
        memberRepository.saveAll(memberList);
        authRepository.saveAll(memberAndAuthFixture.authList());
        List<Classification> classificationFixture = ClassificationFixture.createClassifications();
        classificationRepository.saveAll(classificationFixture);

        productList = ProductFixture.createProductFixtureList(10, classificationFixture.get(0));
        productRepository.saveAll(productList);

        MemberAndAuthFixtureDTO adminFixture = MemberAndAuthFixture.createAdmin();
        admin = adminFixture.memberList().get(0);
        memberRepository.save(admin);
        authRepository.save(adminFixture.authList().get(0));

        newProductQnAList = ProductQnAFixture.createDefaultProductQnA(memberList, productList);
        List<Member> completeQnAMemberFixture = memberList.stream().limit(5).toList();
        List<Product> completeQnAProductFixture = productList.stream().limit(5).toList();
        List<ProductQnA> completeProductQnA = ProductQnAFixture.createProductQnACompletedAnswer(completeQnAMemberFixture, completeQnAProductFixture);
        allProductQnA = new ArrayList<>(newProductQnAList);
        allProductQnA.addAll(completeProductQnA);
        productQnARepository.saveAll(allProductQnA);

        productQnAReplyList = ProductQnAFixture.createDefaultProductQnaReply(admin, completeProductQnA);
        productQnAReplyRepository.saveAll(productQnAReplyList);
    }

    @AfterEach
    void cleanUp() {
        notificationRepository.deleteAll();
        productQnAReplyRepository.deleteAll();
        productQnARepository.deleteAll();
        productRepository.deleteAll();
        classificationRepository.deleteAll();
        authRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName(value = "상품 문의 답변 완료 처리")
    void patchProductQnAComplete() {
        ProductQnA newProductQnAFixture = newProductQnAList.get(0);

        String result = assertDoesNotThrow(() -> adminProductQnAWriteUseCase.patchProductQnAComplete(newProductQnAFixture.getId()));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result);

        ProductQnA patchFixture = productQnARepository.findById(newProductQnAFixture.getId()).orElse(null);

        assertNotNull(patchFixture);
        assertTrue(patchFixture.isProductQnAStat());
    }

    @Test
    @DisplayName(value = "상품 문의 답변 완료 처리. 데이터가 없는 경우")
    void patchProductQnACompleteNotFound() {
        assertThrows(
                IllegalArgumentException.class,
                () -> adminProductQnAWriteUseCase.patchProductQnAComplete(0L)
        );
    }

    @Test
    @DisplayName(value = "상품 문의 답변 작성")
    void postProductQnAReply() {
        ProductQnA newProductQnAFixture = newProductQnAList.get(0);
        String content = "test Reply Content";
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(newProductQnAFixture.getId(), content);

        doNothing().when(productQnAExternalService).sendProductQnANotification(any(ProductQnA.class));

        String result = assertDoesNotThrow(() -> adminProductQnAWriteUseCase.postProductQnAReply(insertDTO, admin.getUserId()));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result);

        List<QnADetailReplyDTO> saveReplyList = productQnAReplyRepository.findAllByQnAId(newProductQnAFixture.getId());

        assertFalse(saveReplyList.isEmpty());

        QnADetailReplyDTO saveReply = saveReplyList.get(0);

        assertEquals(admin.getNickname(), saveReply.writer());
        assertEquals(content, saveReply.replyContent());

        ProductQnA patchProductQnA = productQnARepository.findById(newProductQnAFixture.getId()).orElse(null);

        assertNotNull(patchProductQnA);
        assertTrue(patchProductQnA.isProductQnAStat());
    }

    @Test
    @DisplayName(value = "상품 문의 답변 작성. 상품 문의 데이터가 존재하지 않는 경우")
    void postProductQnAReplyNotFound() {
        String content = "test Reply Content";
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(0L, content);

        assertThrows(
                IllegalArgumentException.class,
                () -> adminProductQnAWriteUseCase.postProductQnAReply(insertDTO, admin.getUserId())
        );
    }

    @Test
    @DisplayName(value = "상품 문의 답변 수정")
    void patchProductQnAReply() {
        ProductQnAReply reply = productQnAReplyList.get(0);
        String content = "patch reply content";
        QnAReplyPatchDTO replyDTO = new QnAReplyPatchDTO(reply.getId(), content);

        String result = assertDoesNotThrow(() -> adminProductQnAWriteUseCase.patchProductQnAReply(replyDTO, admin.getUserId()));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result);

        ProductQnAReply patchReply = productQnAReplyRepository.findById(reply.getId()).orElse(null);

        assertNotNull(patchReply);
        assertEquals(content, patchReply.getReplyContent());
    }

    @Test
    @DisplayName(value = "상품 문의 답변 수정. 답변 데이터가 없는 경우")
    void patchProductQnAReplyNotFound() {
        String content = "patch reply content";
        QnAReplyPatchDTO replyDTO = new QnAReplyPatchDTO(0L, content);

        assertThrows(
                IllegalArgumentException.class,
                () -> adminProductQnAWriteUseCase.patchProductQnAReply(replyDTO, admin.getUserId())
        );
    }

    @Test
    @DisplayName(value = "상품 문의 답변 수정. 작성자가 일치하지 않는 경우")
    void patchProductQnAReplyNotEqualsWriter() {
        ProductQnAReply reply = productQnAReplyList.get(0);
        String content = "patch reply content";
        QnAReplyPatchDTO replyDTO = new QnAReplyPatchDTO(reply.getId(), content);

        assertThrows(
                IllegalArgumentException.class,
                () -> adminProductQnAWriteUseCase.patchProductQnAReply(replyDTO, "writer")
        );
    }
}
