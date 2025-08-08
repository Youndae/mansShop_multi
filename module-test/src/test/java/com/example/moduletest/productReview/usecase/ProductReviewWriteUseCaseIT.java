package com.example.moduletest.productReview.usecase;

import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.fixture.*;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleorder.repository.ProductOrderRepository;
import com.example.moduleproduct.model.dto.productReview.in.MyPagePatchReviewDTO;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import com.example.moduleproduct.repository.productReview.ProductReviewRepository;
import com.example.moduleproduct.repository.productReviewReply.ProductReviewReplyRepository;
import com.example.moduleproduct.usecase.productReview.ProductReviewWriteUseCase;
import com.example.moduletest.ModuleTestApplication;
import com.example.moduleuser.repository.MemberRepository;
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
public class ProductReviewWriteUseCaseIT {

    @Autowired
    private ProductReviewWriteUseCase productReviewWriteUseCase;

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
    private ProductOrderRepository productOrderRepository;

    private List<Member> memberList;

    private Member member;

    private List<Product> productList;

    private List<ProductOption> productOptionList;

    private List<ProductReview> allProductReviewList;

    private List<ProductReviewReply> productReviewReplyList;

    private List<ProductOrder> productOrderList;

    @BeforeEach
    void init() {
        MemberAndAuthFixtureDTO memberAndAuthFixture = MemberAndAuthFixture.createDefaultMember(2);
        MemberAndAuthFixtureDTO adminFixture = MemberAndAuthFixture.createAdmin();
        Member admin = adminFixture.memberList().get(0);
        memberList = memberAndAuthFixture.memberList();
        member = memberList.get(0);
        List<Member> saveMemberList = new ArrayList<>(memberList);
        saveMemberList.addAll(adminFixture.memberList());
        memberRepository.saveAll(saveMemberList);

        List<Classification> classificationList = ClassificationFixture.createClassifications();
        classificationRepository.saveAll(classificationList);

        productList = ProductFixture.createProductFixtureList(50, classificationList.get(0));
        productOptionList = productList.stream().flatMap(v -> v.getProductOptions().stream()).toList();
        productRepository.saveAll(productList);
        productOptionRepository.saveAll(productOptionList);

        List<ProductReview> productReviewList = ProductReviewFixture.createReviewWithCompletedAnswer(memberList, productOptionList);
        productReviewReplyList = ProductReviewFixture.createDefaultReviewReply(productReviewList, admin);
        List<ProductReview> newProductReviewList = ProductReviewFixture.createDefaultReview(memberList, productOptionList);
        allProductReviewList = new ArrayList<>(productReviewList);
        allProductReviewList.addAll(newProductReviewList);

        productReviewRepository.saveAll(allProductReviewList);
        productReviewReplyRepository.saveAll(productReviewReplyList);

        productOrderList = ProductOrderFixture.createDefaultProductOrder(memberList, productOptionList);
        productOrderRepository.saveAll(productOrderList);
    }

    @Test
    @DisplayName(value = "리뷰 수정")
    void patchReview() {
        ProductReview fixture = allProductReviewList.get(0);
        String userId = fixture.getMember().getUserId();
        MyPagePatchReviewDTO reviewDTO = new MyPagePatchReviewDTO(
                fixture.getId(),
                "test patch review content"
        );

        String result = assertDoesNotThrow(() -> productReviewWriteUseCase.patchReview(reviewDTO, userId));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result);

        ProductReview patchReview = productReviewRepository.findById(fixture.getId()).orElse(null);

        assertNotNull(patchReview);
        assertEquals(reviewDTO.content(), patchReview.getReviewContent());
    }

    @Test
    @DisplayName(value = "리뷰 수정. 리뷰 아이디가 잘못 된 경우")
    void patchReviewWrongId() {
        MyPagePatchReviewDTO reviewDTO = new MyPagePatchReviewDTO(
                0L,
                "test patch review content"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> productReviewWriteUseCase.patchReview(reviewDTO, member.getUserId())
        );
    }

    @Test
    @DisplayName(value = "리뷰 수정. 작성자가 일치하지 않는 경우")
    void patchReviewWriterNotEquals() {
        ProductReview fixture = allProductReviewList.get(0);
        String userId = "noneUser";
        MyPagePatchReviewDTO reviewDTO = new MyPagePatchReviewDTO(
                fixture.getId(),
                "test patch review content"
        );

        assertThrows(
                CustomAccessDeniedException.class,
                () -> productReviewWriteUseCase.patchReview(reviewDTO, userId)
        );
    }

    @Test
    @DisplayName(value = "리뷰 삭제")
    void deleteReview() {
        ProductReview fixture = allProductReviewList.get(0);
        long reviewId = fixture.getId();
        String userId = fixture.getMember().getUserId();

        String result = assertDoesNotThrow(() -> productReviewWriteUseCase.deleteReview(reviewId, userId));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result);

        ProductReview deleteReview = productReviewRepository.findById(reviewId).orElse(null);

        assertNull(deleteReview);
    }

    @Test
    @DisplayName(value = "리뷰 삭제. 리뷰 아이디가 잘못 된 경우")
    void deleteReviewWrongId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> productReviewWriteUseCase.deleteReview(0L, member.getUserId())
        );
    }

    @Test
    @DisplayName(value = "리뷰 삭제. 작성자가 일치하지 않는 경우")
    void deleteReviewWriterNotEquals() {
        ProductReview fixture = allProductReviewList.get(0);
        long reviewId = fixture.getId();
        String userId = "noneUser";

        assertThrows(
                CustomAccessDeniedException.class,
                () -> productReviewWriteUseCase.deleteReview(reviewId, userId)
        );

        ProductReview deleteReview = productReviewRepository.findById(reviewId).orElse(null);

        assertNotNull(deleteReview);
    }
}
