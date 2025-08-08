package com.example.moduletest.api.usecase;

import com.example.moduleapi.usecase.ReviewWriteUseCase;
import com.example.modulecommon.fixture.*;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.OrderStatus;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleorder.repository.ProductOrderDetailRepository;
import com.example.moduleorder.repository.ProductOrderRepository;
import com.example.moduleproduct.model.dto.productReview.in.MyPagePostReviewDTO;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import com.example.moduleproduct.repository.productReview.ProductReviewRepository;
import com.example.moduleproduct.repository.productReviewReply.ProductReviewReplyRepository;
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
public class ReviewWriteUseCaseIT {

    @Autowired
    private ReviewWriteUseCase reviewWriteUseCase;

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

    @Autowired
    private ProductOrderDetailRepository productOrderDetailRepository;

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
    @DisplayName(value = "리뷰 작성")
    void postReview() {
        ProductOrderDetail fixture = productOrderList.stream()
                .flatMap(v -> v.getProductOrderDetailList().stream())
                .filter(v -> !v.isOrderReviewStatus())
                .toList()
                .get(0);

        ProductOrder orderFixture = fixture.getProductOrder();
        orderFixture.setOrderStat(OrderStatus.COMPLETE.getStatusStr());
        productOrderRepository.save(orderFixture);

        String userId = fixture.getProductOrder().getMember().getUserId();
        MyPagePostReviewDTO postReviewDTO = new MyPagePostReviewDTO(
                fixture.getProduct().getId(),
                "test post review content",
                fixture.getProductOption().getId(),
                fixture.getId()
        );

        String result = assertDoesNotThrow(() -> reviewWriteUseCase.postReview(postReviewDTO, userId));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result);

        ProductReview postReview = productReviewRepository.findFirstByMember_UserIdOrderByIdDesc(userId);

        assertNotNull(postReview);
        assertEquals(postReviewDTO.productId(), postReview.getProduct().getId());
        assertEquals(postReviewDTO.content(), postReview.getReviewContent());
        assertEquals(postReviewDTO.optionId(), postReview.getProductOption().getId());
        assertEquals(userId, postReview.getMember().getUserId());

        ProductOrderDetail patchDetail = productOrderDetailRepository.findById(fixture.getId()).orElse(null);

        assertNotNull(patchDetail);
        assertTrue(patchDetail.isOrderReviewStatus());
    }

    @Test
    @DisplayName(value = "리뷰 작성. 상품 아이디가 잘못 된 경우")
    void postReviewWrongProductId() {
        MyPagePostReviewDTO postReviewDTO = new MyPagePostReviewDTO(
                "noneProductId",
                "test post review content",
                productOptionList.get(0).getId(),
                productOrderList.get(0).getProductOrderDetailList().get(0).getId()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> reviewWriteUseCase.postReview(postReviewDTO, member.getUserId())
        );

        int allReviewSize = productReviewRepository.findAll().size();

        assertEquals(allProductReviewList.size(), allReviewSize);
    }

    @Test
    @DisplayName(value = "리뷰 작성. 상품 옵션 아이디가 잘못 된 경우")
    void postReviewWrongProductOptionId() {
        MyPagePostReviewDTO postReviewDTO = new MyPagePostReviewDTO(
                productList.get(0).getId(),
                "test post review content",
                0L,
                productOrderList.get(0).getProductOrderDetailList().get(0).getId()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> reviewWriteUseCase.postReview(postReviewDTO, member.getUserId())
        );

        int allReviewSize = productReviewRepository.findAll().size();

        assertEquals(allProductReviewList.size(), allReviewSize);
    }

    @Test
    @DisplayName(value = "리뷰 작성. 주문 상세 아이디가 잘못 된 경우")
    void postReviewWrongProductOrderDetailId() {
        MyPagePostReviewDTO postReviewDTO = new MyPagePostReviewDTO(
                productList.get(0).getId(),
                "test post review content",
                productOptionList.get(0).getId(),
                0L
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> reviewWriteUseCase.postReview(postReviewDTO, member.getUserId())
        );

        int allReviewSize = productReviewRepository.findAll().size();

        assertEquals(allProductReviewList.size(), allReviewSize);
    }
}
