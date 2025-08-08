package com.example.moduletest.product.usecase;

import com.example.modulecommon.fixture.*;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.utils.PaginationUtils;
import com.example.modulecommon.utils.ProductDiscountUtils;
import com.example.moduleproduct.model.dto.page.ProductDetailPageDTO;
import com.example.moduleproduct.model.dto.product.business.ProductQnAResponseDTO;
import com.example.moduleproduct.model.dto.product.out.ProductDetailDTO;
import com.example.moduleproduct.model.dto.product.out.ProductDetailReviewDTO;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productInfoImage.ProductInfoImageRepository;
import com.example.moduleproduct.repository.productLike.ProductLikeRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import com.example.moduleproduct.repository.productQnA.ProductQnARepository;
import com.example.moduleproduct.repository.productQnAReply.ProductQnAReplyRepository;
import com.example.moduleproduct.repository.productReview.ProductReviewRepository;
import com.example.moduleproduct.repository.productReviewReply.ProductReviewReplyRepository;
import com.example.moduleproduct.repository.productThumbnail.ProductThumbnailRepository;
import com.example.moduleproduct.usecase.product.ProductReadUseCase;
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
public class ProductReadUseCaseIT {

    @Autowired
    private ProductReadUseCase productReadUseCase;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ClassificationRepository classificationRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductOptionRepository productOptionRepository;

    @Autowired
    private ProductThumbnailRepository productThumbnailRepository;

    @Autowired
    private ProductInfoImageRepository productInfoImageRepository;

    @Autowired
    private ProductLikeRepository productLikeRepository;

    @Autowired
    private ProductReviewRepository productReviewRepository;

    @Autowired
    private ProductReviewReplyRepository productReviewReplyRepository;

    @Autowired
    private ProductQnARepository productQnARepository;

    @Autowired
    private ProductQnAReplyRepository productQnAReplyRepository;

    @Autowired
    private EntityManager em;

    private Member member;

    private List<Member> memberList;

    private Product product;

    private ProductLike productLike;

    private List<ProductReview> answerReviewList;

    private List<ProductReview> newReviewList;

    private List<ProductReview> allReviewList;

    private List<ProductReviewReply> reviewReplyList;

    private List<ProductQnA> answerProductQnAList;

    private List<ProductQnA> newProductQnAList;

    private List<ProductQnA> allProductQnAList;

    private List<ProductQnAReply> productQnAReplyList;

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
        productOptionRepository.saveAll(product.getProductOptions());
        productThumbnailRepository.saveAll(product.getProductThumbnails());
        productInfoImageRepository.saveAll(product.getProductInfoImages());

        productLike = ProductLikeFixture.createDefaultProductLike(List.of(member), List.of(product)).get(0);
        productLikeRepository.save(productLike);

        answerReviewList = ProductReviewFixture.createReviewWithCompletedAnswer(memberList, product.getProductOptions());
        newReviewList = ProductReviewFixture.createDefaultReview(List.of(member), product.getProductOptions());
        reviewReplyList = ProductReviewFixture.createDefaultReviewReply(answerReviewList, admin);

        allReviewList = new ArrayList<>(answerReviewList);
        allReviewList.addAll(newReviewList);
        productReviewRepository.saveAll(allReviewList);
        productReviewReplyRepository.saveAll(reviewReplyList);

        answerProductQnAList = ProductQnAFixture.createProductQnACompletedAnswer(memberList, List.of(product));
        newProductQnAList = ProductQnAFixture.createDefaultProductQnA(memberList, List.of(product));
        productQnAReplyList = ProductQnAFixture.createDefaultProductQnaReply(admin, answerProductQnAList);

        allProductQnAList = new ArrayList<>(answerProductQnAList);
        allProductQnAList.addAll(newProductQnAList);
        productQnARepository.saveAll(allProductQnAList);
        productQnAReplyRepository.saveAll(productQnAReplyList);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName(value = "상품 상세 정보 조회. 로그인 상태고 해당 상품을 관심상품으로 등록한 경우")
    void getDetailLikeProduct() {
        int discountPrice = ProductDiscountUtils.calcDiscountPrice(product.getProductPrice(), product.getProductDiscount());
        ProductDetailPageDTO pageDTO = new ProductDetailPageDTO();
        int reviewContentSize = Math.min(allReviewList.size(), pageDTO.reviewAmount());
        int reviewTotalPages = PaginationUtils.getTotalPages(allReviewList.size(), pageDTO.reviewAmount());
        int qnaContentSize = Math.min(allProductQnAList.size(), pageDTO.qnaAmount());
        int qnaTotalPages = PaginationUtils.getTotalPages(allProductQnAList.size(), pageDTO.qnaAmount());
        List<ProductOption> options = product.getProductOptions()
                .stream()
                .filter(ProductOption::isOpen)
                .toList();

        ProductDetailDTO result = assertDoesNotThrow(() -> productReadUseCase.getProductDetail(product.getId(), member.getUserId()));

        assertNotNull(result);
        assertEquals(product.getId(), result.productId());
        assertEquals(product.getProductName(), result.productName());
        assertEquals(product.getProductPrice(), result.productPrice());
        assertEquals(product.getThumbnail(), result.productImageName());
        assertTrue(result.likeStat());
        assertEquals(product.getProductDiscount(), result.discount());
        assertEquals(discountPrice, result.discountPrice());
        assertEquals(options.size(), result.productOptionList().size());
        assertEquals(product.getProductThumbnails().size(), result.productThumbnailList().size());
        for(int i = 0; i < product.getProductThumbnails().size(); i++) {
            String thumbnailName = product.getProductThumbnails().get(i).getImageName();
            String resultThumbnailName = result.productThumbnailList().get(i);

            assertEquals(thumbnailName, resultThumbnailName);
        }
        assertEquals(product.getProductInfoImages().size(), result.productInfoImageList().size());
        for(int i = 0; i < product.getProductInfoImages().size(); i++) {
            String infoImageName = product.getProductInfoImages().get(i).getImageName();
            String resultInfoImageName = result.productInfoImageList().get(i);

            assertEquals(infoImageName, resultInfoImageName);
        }
        assertFalse(result.productReviewList().empty());
        assertFalse(result.productReviewList().content().isEmpty());
        assertEquals(reviewContentSize, result.productReviewList().content().size());
        assertEquals(reviewTotalPages, result.productReviewList().totalPages());
        assertEquals(allReviewList.size(), result.productReviewList().totalElements());

        assertFalse(result.productQnAList().empty());
        assertFalse(result.productQnAList().content().isEmpty());
        assertEquals(qnaContentSize, result.productQnAList().content().size());
        assertEquals(qnaTotalPages, result.productQnAList().totalPages());
        assertEquals(allProductQnAList.size(), result.productQnAList().totalElements());
    }

    @Test
    @DisplayName(value = "상품 상세 정보 조회. 로그인 상태고 해당 상품을 관심상품으로 등록하지 않은 경우")
    void getDetailDeLikeProduct() {
        int discountPrice = ProductDiscountUtils.calcDiscountPrice(product.getProductPrice(), product.getProductDiscount());
        ProductDetailPageDTO pageDTO = new ProductDetailPageDTO();
        int reviewContentSize = Math.min(allReviewList.size(), pageDTO.reviewAmount());
        int reviewTotalPages = PaginationUtils.getTotalPages(allReviewList.size(), pageDTO.reviewAmount());
        int qnaContentSize = Math.min(allProductQnAList.size(), pageDTO.qnaAmount());
        int qnaTotalPages = PaginationUtils.getTotalPages(allProductQnAList.size(), pageDTO.qnaAmount());
        List<ProductOption> options = product.getProductOptions()
                .stream()
                .filter(ProductOption::isOpen)
                .toList();

        ProductDetailDTO result = assertDoesNotThrow(() -> productReadUseCase.getProductDetail(product.getId(), "noneUserId"));

        assertNotNull(result);
        assertEquals(product.getId(), result.productId());
        assertEquals(product.getProductName(), result.productName());
        assertEquals(product.getProductPrice(), result.productPrice());
        assertEquals(product.getThumbnail(), result.productImageName());
        assertFalse(result.likeStat());
        assertEquals(product.getProductDiscount(), result.discount());
        assertEquals(discountPrice, result.discountPrice());
        assertEquals(options.size(), result.productOptionList().size());
        assertEquals(product.getProductThumbnails().size(), result.productThumbnailList().size());
        for(int i = 0; i < product.getProductThumbnails().size(); i++) {
            String thumbnailName = product.getProductThumbnails().get(i).getImageName();
            String resultThumbnailName = result.productThumbnailList().get(i);

            assertEquals(thumbnailName, resultThumbnailName);
        }
        assertEquals(product.getProductInfoImages().size(), result.productInfoImageList().size());
        for(int i = 0; i < product.getProductInfoImages().size(); i++) {
            String infoImageName = product.getProductInfoImages().get(i).getImageName();
            String resultInfoImageName = result.productInfoImageList().get(i);

            assertEquals(infoImageName, resultInfoImageName);
        }
        assertFalse(result.productReviewList().empty());
        assertFalse(result.productReviewList().content().isEmpty());
        assertEquals(reviewContentSize, result.productReviewList().content().size());
        assertEquals(reviewTotalPages, result.productReviewList().totalPages());
        assertEquals(allReviewList.size(), result.productReviewList().totalElements());

        assertFalse(result.productQnAList().empty());
        assertFalse(result.productQnAList().content().isEmpty());
        assertEquals(qnaContentSize, result.productQnAList().content().size());
        assertEquals(qnaTotalPages, result.productQnAList().totalPages());
        assertEquals(allProductQnAList.size(), result.productQnAList().totalElements());
    }

    @Test
    @DisplayName(value = "상품 상세 정보 조회. 비회원인 경우")
    void getDetailAnonymous() {
        int discountPrice = ProductDiscountUtils.calcDiscountPrice(product.getProductPrice(), product.getProductDiscount());
        ProductDetailPageDTO pageDTO = new ProductDetailPageDTO();
        int reviewContentSize = Math.min(allReviewList.size(), pageDTO.reviewAmount());
        int reviewTotalPages = PaginationUtils.getTotalPages(allReviewList.size(), pageDTO.reviewAmount());
        int qnaContentSize = Math.min(allProductQnAList.size(), pageDTO.qnaAmount());
        int qnaTotalPages = PaginationUtils.getTotalPages(allProductQnAList.size(), pageDTO.qnaAmount());
        List<ProductOption> options = product.getProductOptions()
                .stream()
                .filter(ProductOption::isOpen)
                .toList();

        ProductDetailDTO result = assertDoesNotThrow(() -> productReadUseCase.getProductDetail(product.getId(), null));

        assertNotNull(result);
        assertEquals(product.getId(), result.productId());
        assertEquals(product.getProductName(), result.productName());
        assertEquals(product.getProductPrice(), result.productPrice());
        assertEquals(product.getThumbnail(), result.productImageName());
        assertFalse(result.likeStat());
        assertEquals(product.getProductDiscount(), result.discount());
        assertEquals(discountPrice, result.discountPrice());
        assertEquals(options.size(), result.productOptionList().size());
        assertEquals(product.getProductThumbnails().size(), result.productThumbnailList().size());
        for(int i = 0; i < product.getProductThumbnails().size(); i++) {
            String thumbnailName = product.getProductThumbnails().get(i).getImageName();
            String resultThumbnailName = result.productThumbnailList().get(i);

            assertEquals(thumbnailName, resultThumbnailName);
        }
        assertEquals(product.getProductInfoImages().size(), result.productInfoImageList().size());
        for(int i = 0; i < product.getProductInfoImages().size(); i++) {
            String infoImageName = product.getProductInfoImages().get(i).getImageName();
            String resultInfoImageName = result.productInfoImageList().get(i);

            assertEquals(infoImageName, resultInfoImageName);
        }
        assertFalse(result.productReviewList().empty());
        assertFalse(result.productReviewList().content().isEmpty());
        assertEquals(reviewContentSize, result.productReviewList().content().size());
        assertEquals(reviewTotalPages, result.productReviewList().totalPages());
        assertEquals(allReviewList.size(), result.productReviewList().totalElements());

        assertFalse(result.productQnAList().empty());
        assertFalse(result.productQnAList().content().isEmpty());
        assertEquals(qnaContentSize, result.productQnAList().content().size());
        assertEquals(qnaTotalPages, result.productQnAList().totalPages());
        assertEquals(allProductQnAList.size(), result.productQnAList().totalElements());
    }

    @Test
    @DisplayName(value = "상품 상세 정보 조회. 상품 아이디가 잘못 된 경우")
    void getDetailWrongId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> productReadUseCase.getProductDetail("WrongProductId", null)
        );
    }

    @Test
    @DisplayName(value = "리뷰 리스트 조회")
    void getDetailReview() {
        ProductDetailPageDTO pageDTO = new ProductDetailPageDTO();
        int contentSize = Math.min(allReviewList.size(), pageDTO.reviewAmount());
        int totalPages = PaginationUtils.getTotalPages(allReviewList.size(), pageDTO.reviewAmount());

        Page<ProductDetailReviewDTO> result = assertDoesNotThrow(() -> productReadUseCase.getProductDetailReview(pageDTO, product.getId()));

        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        assertFalse(result.isEmpty());
        assertEquals(contentSize, result.getContent().size());
        assertEquals(totalPages, result.getTotalPages());
        assertEquals(allReviewList.size(), result.getTotalElements());
    }

    @Test
    @DisplayName(value = "리뷰 리스트 조회. 데이터가 없는 경우")
    void getDetailReviewEmpty() {
        productReviewRepository.deleteAll();

        ProductDetailPageDTO pageDTO = new ProductDetailPageDTO();

        Page<ProductDetailReviewDTO> result = assertDoesNotThrow(() -> productReadUseCase.getProductDetailReview(pageDTO, product.getId()));

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertTrue(result.isEmpty());
        assertEquals(0, result.getContent().size());
        assertEquals(0, result.getTotalPages());
    }

    @Test
    @DisplayName(value = "상품 문의 리스트 조회")
    void getDetailQnA() {
        ProductDetailPageDTO pageDTO = new ProductDetailPageDTO();
        int contentSize = Math.min(allProductQnAList.size(), pageDTO.qnaAmount());
        int totalPages = PaginationUtils.getTotalPages(allProductQnAList.size(), pageDTO.qnaAmount());

        Page<ProductQnAResponseDTO> result = assertDoesNotThrow(() -> productReadUseCase.getProductDetailQnA(pageDTO, product.getId()));

        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        assertFalse(result.isEmpty());
        assertEquals(contentSize, result.getContent().size());
        assertEquals(totalPages, result.getTotalPages());
        assertEquals(allProductQnAList.size(), result.getTotalElements());
    }

    @Test
    @DisplayName(value = "상품 문의 리스트 조회. 데이터가 없는 경우")
    void getDetailQnAEmpty() {
        productQnARepository.deleteAll();
        ProductDetailPageDTO pageDTO = new ProductDetailPageDTO();

        Page<ProductQnAResponseDTO> result = assertDoesNotThrow(() -> productReadUseCase.getProductDetailQnA(pageDTO, product.getId()));

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertTrue(result.isEmpty());
        assertEquals(0, result.getContent().size());
        assertEquals(0, result.getTotalPages());
    }
}
