package com.example.moduletest.admin.productReview.usecase;

import com.example.modulecommon.fixture.ClassificationFixture;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.fixture.ProductReviewFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.Result;
import com.example.modulenotification.repository.NotificationRepository;
import com.example.moduleproduct.model.dto.admin.review.in.AdminReviewReplyRequestDTO;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import com.example.moduleproduct.repository.productReview.ProductReviewRepository;
import com.example.moduleproduct.repository.productReviewReply.ProductReviewReplyRepository;
import com.example.moduleproduct.service.productReview.ProductReviewExternalService;
import com.example.moduleproduct.usecase.admin.productReview.AdminProductReviewWriteUseCase;
import com.example.moduletest.ModuleTestApplication;
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

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ModuleTestApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
public class AdminProductReviewWriteUseCaseIT {

    @Autowired
    private AdminProductReviewWriteUseCase adminProductReviewWriteUseCase;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ClassificationRepository classificationRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductOptionRepository productOptionRepository;

    @Autowired
    private ProductReviewRepository productReviewRepository;

    @Autowired
    private ProductReviewReplyRepository productReviewReplyRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @MockitoBean
    private ProductReviewExternalService productReviewExternalService;

    private List<Member> memberList;

    private List<Product> productList;

    private List<ProductReview> newProductReviewList;

    private List<ProductReview> allProductReviewList;

    private List<ProductReviewReply> productReviewReplyList;

    private Member admin;

    @BeforeEach
    void init() {
        MemberAndAuthFixtureDTO memberAndAuthFixture = MemberAndAuthFixture.createDefaultMember(10);
        memberList = memberAndAuthFixture.memberList();
        memberRepository.saveAll(memberList);

        MemberAndAuthFixtureDTO adminFixture = MemberAndAuthFixture.createAdmin();
        admin = adminFixture.memberList().get(0);
        memberRepository.save(admin);

        List<Classification> classificationList = ClassificationFixture.createClassifications();
        classificationRepository.saveAll(classificationList);
        productList = ProductFixture.createProductFixtureList(5, classificationList.get(0));
        List<ProductOption> optionList = productList.stream().flatMap(v -> v.getProductOptions().stream()).toList();
        productRepository.saveAll(productList);
        productOptionRepository.saveAll(optionList);

        newProductReviewList = ProductReviewFixture.createDefaultReview(memberList, optionList);
        List<ProductReview> answerCompleteReviewList = ProductReviewFixture.createReviewWithCompletedAnswer(memberList, optionList);
        allProductReviewList = new ArrayList<>(answerCompleteReviewList);
        allProductReviewList.addAll(newProductReviewList);

        productReviewRepository.saveAll(allProductReviewList);

        productReviewReplyList = ProductReviewFixture.createDefaultReviewReply(answerCompleteReviewList, admin);
        productReviewReplyRepository.saveAll(productReviewReplyList);
    }

    @AfterEach
    void cleanUp() {
        notificationRepository.deleteAll();
        productReviewReplyRepository.deleteAll();
        productReviewRepository.deleteAll();
        productOptionRepository.deleteAll();
        productRepository.deleteAll();
        classificationRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName(value = "리뷰 답변 작성")
    void postReviewReply() {
        ProductReview reviewFixture = allProductReviewList.stream().filter(v -> !v.isStatus()).findFirst().get();
        String content = "test Review Reply";
        AdminReviewReplyRequestDTO postDTO = new AdminReviewReplyRequestDTO(reviewFixture.getId(), content);

        doNothing().when(productReviewExternalService).sendProductReviewNotification(any());

        assertDoesNotThrow(() -> adminProductReviewWriteUseCase.postReviewReply(postDTO, admin.getUserId()));

        ProductReview reviewEntity = productReviewRepository.findById(reviewFixture.getId()).orElse(null);

        assertNotNull(reviewEntity);
        assertTrue(reviewEntity.isStatus());

        ProductReviewReply checkEntity = productReviewReplyRepository.findByReviewId(reviewFixture.getId());

        assertNotNull(checkEntity);
        assertEquals(content, checkEntity.getReplyContent());
    }

    @Test
    @DisplayName(value = "리뷰 답변 작성. ProductReview 데이터가 없는 경우.")
    void postReviewReplyReviewNotFound() {
        String content = "test Review Reply";
        AdminReviewReplyRequestDTO postDTO = new AdminReviewReplyRequestDTO(0L, content);
        assertThrows(
                IllegalArgumentException.class,
                () -> adminProductReviewWriteUseCase.postReviewReply(postDTO, admin.getUserId())
        );
    }

    @Test
    @DisplayName(value = "리뷰 답변 작성. 이미 답변이 작성된 리뷰인 경우")
    void postReviewReplyAlreadyExist() {
        Long reviewId = allProductReviewList.stream().filter(ProductReview::isStatus).findFirst().get().getId();
        String content = "test Review Reply";
        AdminReviewReplyRequestDTO postDTO = new AdminReviewReplyRequestDTO(reviewId, content);
        assertThrows(
                IllegalArgumentException.class,
                () -> adminProductReviewWriteUseCase.postReviewReply(postDTO, admin.getUserId())
        );
    }
}
