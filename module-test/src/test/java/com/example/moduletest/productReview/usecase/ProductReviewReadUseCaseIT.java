package com.example.moduletest.productReview.usecase;

import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.fixture.ClassificationFixture;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.fixture.ProductReviewFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.dto.page.MyPagePageDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.utils.PaginationUtils;
import com.example.moduleproduct.model.dto.productReview.out.MyPagePatchReviewDataDTO;
import com.example.moduleproduct.model.dto.productReview.out.MyPageReviewDTO;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import com.example.moduleproduct.repository.productReview.ProductReviewRepository;
import com.example.moduleproduct.repository.productReviewReply.ProductReviewReplyRepository;
import com.example.moduleproduct.usecase.productReview.ProductReviewReadUseCase;
import com.example.moduletest.ModuleTestApplication;
import com.example.moduleuser.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.core.RedisTemplate;
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
public class ProductReviewReadUseCaseIT {

    @Autowired
    private ProductReviewReadUseCase productReviewReadUseCase;

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
    private RedisTemplate<String, Long> redisTemplate;

    private List<Member> memberList;

    private Member member;

    private List<Product> productList;

    private List<ProductOption> productOptionList;

    private List<ProductReview> allProductReviewList;

    private List<ProductReviewReply> productReviewReplyList;

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
    }

    private List<ProductReview> getMemberProductReviewList(Member member, int limit) {
        int size = getLimitSize(allProductReviewList.size(), limit);

        return allProductReviewList.stream()
                .filter(v -> userIdEquals(v.getMember(), member))
                .limit(size)
                .toList();
    }

    private int getLimitSize(int listSize, int limit) {
        return limit == 0 ? listSize : limit;
    }

    private boolean userIdEquals(Member listMember, Member member) {
        return listMember.getUserId().equals(member.getUserId());
    }

    @Test
    @DisplayName(value = "리뷰 목록 조회")
    void getReview() {
        List<ProductReview> fixtureList = getMemberProductReviewList(member, 0);
        MyPagePageDTO pageDTO = new MyPagePageDTO(1);
        int totalPages = PaginationUtils.getTotalPages(fixtureList.size(), pageDTO.amount());
        int contentSize = Math.min(fixtureList.size(), pageDTO.amount());

        Page<MyPageReviewDTO> result = assertDoesNotThrow(() -> productReviewReadUseCase.getMyPageReviewList(pageDTO, member.getUserId()));

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertFalse(result.getContent().isEmpty());
        assertEquals(fixtureList.size(), result.getTotalElements());
        assertEquals(totalPages, result.getTotalPages());
        assertEquals(contentSize, result.getContent().size());
    }

    @Test
    @DisplayName(value = "리뷰 목록 조회. 데이터가 없는 경우")
    void getReviewEmpty() {
        productReviewReplyRepository.deleteAll();
        productReviewRepository.deleteAll();
        MyPagePageDTO pageDTO = new MyPagePageDTO(1);

        Page<MyPageReviewDTO> result = assertDoesNotThrow(() -> productReviewReadUseCase.getMyPageReviewList(pageDTO, member.getUserId()));

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
    }

    @Test
    @DisplayName(value = "리뷰 수정을 위한 조회")
    void getPatchReview() {
        ProductReview fixture = allProductReviewList.get(0);

        MyPagePatchReviewDataDTO result = assertDoesNotThrow(() -> productReviewReadUseCase.getPatchReview(fixture.getId(), fixture.getMember().getUserId()));

        assertNotNull(result);
        assertEquals(fixture.getId(), result.reviewId());
        assertEquals(fixture.getReviewContent(), result.content());
        assertEquals(fixture.getProduct().getProductName(), result.productName());
    }

    @Test
    @DisplayName(value = "리뷰 수정을 위한 조회. 리뷰 아이디가 잘못 된 경우")
    void getPatchReviewWrongId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> productReviewReadUseCase.getPatchReview(0L, member.getUserId())
        );
    }

    @Test
    @DisplayName(value = "리뷰 수정을 위한 조회. 작성자가 일치하지 않는 경우")
    void getPatchReviewWriterNotEquals() {
        ProductReview fixture = allProductReviewList.get(0);

        assertThrows(
                CustomAccessDeniedException.class,
                () -> productReviewReadUseCase.getPatchReview(fixture.getId(), "wrongUserId")
        );
    }
}
