package com.example.moduletest.admin.productReview.usecase;

import com.example.modulecommon.fixture.ClassificationFixture;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.fixture.ProductReviewFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.AdminListType;
import com.example.modulecommon.utils.TestPaginationUtils;
import com.example.moduleproduct.model.dto.admin.review.out.AdminReviewDTO;
import com.example.moduleproduct.model.dto.admin.review.out.AdminReviewDetailDTO;
import com.example.moduleproduct.model.dto.page.AdminReviewPageDTO;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import com.example.moduleproduct.repository.productReview.ProductReviewRepository;
import com.example.moduleproduct.repository.productReviewReply.ProductReviewReplyRepository;
import com.example.moduleproduct.usecase.admin.productReview.AdminProductReviewReadUseCase;
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
public class AdminProductReviewReadUseCaseIT {

    @Autowired
    private AdminProductReviewReadUseCase adminProductReviewReadUseCase;

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

    private List<Member> memberList;

    private List<Product> productList;

    private List<ProductReview> newProductReviewList;

    private List<ProductReview> allProductReviewList;

    private List<ProductReviewReply> productReviewReplyList;

    private Member admin;

    private static String SEARCH_TYPE_BY_USER = "user";

    private static String SEARCH_TYPE_BY_PRODUCT_NAME = "product";

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

    @Test
    @DisplayName(value = "전체 리뷰 리스트 조회")
    void getAllReviewList() {
        AdminReviewPageDTO pageDTO = new AdminReviewPageDTO(1);
        AdminListType listType = AdminListType.ALL;
        int totalPages = TestPaginationUtils.getTotalPages(allProductReviewList.size(), pageDTO.amount());
        int resultContentSize = Math.min(allProductReviewList.size(), pageDTO.amount());
        PagingListDTO<AdminReviewDTO> result = assertDoesNotThrow(() -> adminProductReviewReadUseCase.getReviewList(pageDTO, listType, allProductReviewList.size()));

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertFalse(result.pagingData().isEmpty());
        assertEquals(resultContentSize, result.content().size());
        assertEquals(allProductReviewList.size(), result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());
    }

    @Test
    @DisplayName(value = "전체 리뷰 리스트 조회. 데이터가 없는 경우")
    void getAllReviewListEmpty() {
        productReviewReplyRepository.deleteAll();
        productReviewRepository.deleteAll();
        AdminReviewPageDTO pageDTO = new AdminReviewPageDTO(1);
        AdminListType listType = AdminListType.ALL;

        PagingListDTO<AdminReviewDTO> result = assertDoesNotThrow(() -> adminProductReviewReadUseCase.getReviewList(pageDTO, listType, 0L));

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertTrue(result.pagingData().isEmpty());
        assertEquals(0, result.pagingData().getTotalElements());
        assertEquals(0, result.pagingData().getTotalPages());
    }

    @Test
    @DisplayName(value = "리뷰 리스트 조회. 사용자 이름 또는 닉네임 기반 검색")
    void getAllReviewListSearchUser() {
        Member searchMember = memberList.get(0);
        AdminReviewPageDTO pageDTO = new AdminReviewPageDTO(
                searchMember.getUserName(),
                SEARCH_TYPE_BY_USER,
                1
        );
        AdminListType listType = AdminListType.ALL;
        List<ProductReview> reviewFixtureList = allProductReviewList.stream()
                .filter(v ->
                        v.getMember().getUserName().equals(searchMember.getUserName())
                )
                .toList();
        int totalPages = TestPaginationUtils.getTotalPages(reviewFixtureList.size(), pageDTO.amount());
        int resultContentSize = Math.min(reviewFixtureList.size(), pageDTO.amount());

        PagingListDTO<AdminReviewDTO> result = assertDoesNotThrow(() -> adminProductReviewReadUseCase.getReviewList(pageDTO, listType, 0L));

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertFalse(result.pagingData().isEmpty());
        assertEquals(resultContentSize, result.content().size());
        assertEquals(reviewFixtureList.size(), result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());
    }

    @Test
    @DisplayName(value = "리뷰 리스트 조회. 상품명 기반 검색")
    void getAllReviewListSearchProductName() {
        Product searchProduct = productList.get(0);
        AdminReviewPageDTO pageDTO = new AdminReviewPageDTO(
                searchProduct.getProductName(),
                SEARCH_TYPE_BY_PRODUCT_NAME,
                1
        );
        AdminListType listType = AdminListType.ALL;
        List<ProductReview> reviewFixtureList = allProductReviewList.stream()
                .filter(v ->
                        v.getProduct().getProductName().equals(searchProduct.getProductName())
                )
                .toList();
        int totalPages = TestPaginationUtils.getTotalPages(reviewFixtureList.size(), pageDTO.amount());
        int resultContentSize = Math.min(reviewFixtureList.size(), pageDTO.amount());

        PagingListDTO<AdminReviewDTO> result = assertDoesNotThrow(() -> adminProductReviewReadUseCase.getReviewList(pageDTO, listType, 0L));

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertFalse(result.pagingData().isEmpty());
        assertEquals(resultContentSize, result.content().size());
        assertEquals(reviewFixtureList.size(), result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());
    }

    @Test
    @DisplayName(value = "미답변 리뷰 리스트 조회")
    void getNewReviewList() {
        AdminReviewPageDTO pageDTO = new AdminReviewPageDTO(1);
        AdminListType listType = AdminListType.NEW;
        int totalPages = TestPaginationUtils.getTotalPages(newProductReviewList.size(), pageDTO.amount());
        int resultContentSize = Math.min(newProductReviewList.size(), pageDTO.amount());
        PagingListDTO<AdminReviewDTO> result = assertDoesNotThrow(() -> adminProductReviewReadUseCase.getReviewList(pageDTO, listType, 0L));

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertFalse(result.pagingData().isEmpty());
        assertEquals(resultContentSize, result.content().size());
        assertEquals(newProductReviewList.size(), result.pagingData().getTotalElements());
        assertEquals(totalPages, result.pagingData().getTotalPages());
    }

    @Test
    @DisplayName(value = "미답변 리뷰 리스트 조회. 데이터가 없는 경우")
    void getNewReviewListEmpty() {
        List<Long> newReviewListIds = newProductReviewList.stream().map(ProductReview::getId).toList();
        productReviewRepository.deleteAllById(newReviewListIds);
        AdminReviewPageDTO pageDTO = new AdminReviewPageDTO(1);
        AdminListType listType = AdminListType.NEW;

        PagingListDTO<AdminReviewDTO> result = assertDoesNotThrow(() -> adminProductReviewReadUseCase.getReviewList(pageDTO, listType, 0L));

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertTrue(result.pagingData().isEmpty());
        assertEquals(0, result.pagingData().getTotalElements());
        assertEquals(0, result.pagingData().getTotalPages());
    }

    @Test
    @DisplayName(value = "리뷰 상세 조회")
    void getReviewDetail() {
        ProductReview reviewFixture = allProductReviewList.stream().filter(ProductReview::isStatus).findFirst().get();
        ProductReviewReply reviewReply = productReviewReplyList.stream()
                .filter(v ->
                        v.getProductReview().getId().equals(reviewFixture.getId())
                )
                .findFirst()
                .get();
        AdminReviewDetailDTO result = assertDoesNotThrow(() -> adminProductReviewReadUseCase.getReviewDetail(reviewFixture.getId()));

        assertNotNull(result);
        assertEquals(reviewFixture.getId(), result.reviewId());
        assertEquals(reviewFixture.getProduct().getProductName(), result.productName());
        assertEquals(reviewFixture.getProductOption().getSize(), result.size());
        assertEquals(reviewFixture.getProductOption().getColor(), result.color());
        assertEquals(reviewFixture.getMember().getNickname(), result.writer());
        assertEquals(reviewFixture.getReviewContent(), result.content());
        assertEquals(reviewReply.getReplyContent(), result.replyContent());
    }

    @Test
    @DisplayName(value = "리뷰 상세 조회. 데이터가 없는 경우")
    void getReviewDetailNotFound() {
        assertThrows(
                IllegalArgumentException.class,
                () -> adminProductReviewReadUseCase.getReviewDetail(0L)
        );
    }
}
