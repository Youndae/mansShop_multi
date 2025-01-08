package com.example.moduleproduct.service;

import com.example.moduleauth.repository.MemberRepository;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleconfig.config.exception.customException.CustomNotFoundException;
import com.example.moduleproduct.ModuleProductApplication;
import com.example.moduleproduct.fixture.MemberFixture;
import com.example.moduleproduct.fixture.ProductFixture;
import com.example.moduleproduct.fixture.ProductQnAFixture;
import com.example.moduleproduct.fixture.ProductReviewFixture;
import com.example.moduleproduct.model.dto.page.ProductDetailPageDTO;
import com.example.moduleproduct.model.dto.product.business.*;
import com.example.moduleproduct.model.dto.product.in.ProductQnAPostDTO;
import com.example.moduleproduct.model.dto.product.out.ProductDetailDTO;
import com.example.moduleproduct.model.dto.product.out.ProductPageableDTO;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productLike.ProductLikeRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import com.example.moduleproduct.repository.productQnA.ProductQnARepository;
import com.example.moduleproduct.repository.productQnAReply.ProductQnAReplyRepository;
import com.example.moduleproduct.repository.productReview.ProductReviewRepository;
import com.example.moduleproduct.repository.productReviewReply.ProductReviewReplyRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest(classes = ModuleProductApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("integration-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class ProductServiceIT {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductOptionRepository productOptionRepository;

    @Autowired
    private ClassificationRepository classificationRepository;

    @Autowired
    private MemberRepository memberRepository;

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


    private List<Product> productList;

    private List<ProductReview> reviewList;

    private List<ProductReviewReply> reviewReplies;

    private List<ProductQnA> productQnAList;

    private List<Member> memberList;

    private List<ProductOption> firstProductOptions;

    /**
     * @Fixture
     * Classification
     *  OUTER, TOP 두개
     * Product
     *  저장은 List로 하되 필요한 데이터는 단일.
     * productOption
     *  기본 Product 생성 시 처리되는 옵션 사용.
     * Thumbnail
     *  8개의 데이터. product0, 1에 각각 4개씩만 넣기
     * InfoImage
     *  6개의 데이터. product0, 1에 각각 3개씩 넣기
     * Member
     *  세개의 Member 데이터. 두개는 nickname 작성, 하나는 미작성, 하나는 관리자로.
     * ProductLike
     *  하나의 Member가 갖고 있는 단일 Product의 like
     * Review
     *  20개의 데이터. product0, 1에 각각 10개씩.
     *  각 Product당 member1 5개 member2 5개 분리해서.
     * ReviewReply
     *  10개의 데이터. product0, 1에 각각 5개씩. 전부 Admin
     * QnA
     *  20개의 데이터. product0, 1에 각각 10개씩.
     *  각 Product당 member1 5개 member2 5개 분리해서.
     * QnAReply
     *  20개의 데이터. product0, 1에 각각 10개씩.
     *  10개 중 Admin5, user 5
     *
     */
    @BeforeAll
    void init() {
        List<Classification> classifications = ProductFixture.createClassificationList();
        productList = ProductFixture.createProductList();
        List<ProductThumbnail> thumbnailList = ProductFixture.createProductThumbnailList();
        List<ProductInfoImage> infoImageList = ProductFixture.createProductInfoImageList();
        memberList = List.of(MemberFixture.createOneMember(),
                                        MemberFixture.createUseNicknameMember(2)
                                );
        ProductLike productLike = ProductLike.builder().product(productList.get(0)).member(memberList.get(0)).build();
        reviewList = new ArrayList<>();
        reviewReplies = new ArrayList<>();
        productQnAList = new ArrayList<>();
        List<ProductQnAReply> qnAReplies = new ArrayList<>();

        for(int i = 0; i < thumbnailList.size(); i++){
            int idx = 0;
            if(i > 5)
                idx = 1;

            Product product = productList.get(idx);
            product.addProductThumbnail(thumbnailList.get(i));
            product.addProductInfoImage(infoImageList.get(i));
        }

        for(int i = 0; i < 20; i++) {
            Member member;
            Product product;

            if(i < 5){
                member = memberList.get(0);
                product = productList.get(0);
            }else if(i < 10) {
                member = memberList.get(1);
                product = productList.get(0);
            }else if(i < 15) {
                member = memberList.get(0);
                product = productList.get(1);
            }else {
                member = memberList.get(1);
                product = productList.get(1);
            }

            reviewList.add(ProductReviewFixture.createProductReview(i, member, product));
            productQnAList.add(ProductQnAFixture.createProductQnA(i, member, product));
        }

        //review reply 0 ~ 4 까지 하나, 10 ~ 14까지 하나.
        for(int i = 0; i < 5; i++) {
            ProductReview productReview = reviewList.get(i);
            reviewReplies.add(ProductReviewFixture.createProductReviewReply(productReview.getMember(), productReview, i));
        }

        for(int i = 10; i < 15; i++) {
            ProductReview productReview = reviewList.get(i);
            reviewReplies.add(ProductReviewFixture.createProductReviewReply(productReview.getMember(), productReview, i));
        }
        //qna Reply 9에 10개 10에 10개
        for(int i = 0; i < 10; i++) {
            ProductQnA productQnA9 = productQnAList.get(9);
            ProductQnA productQnA10 = productQnAList.get(10);

            productQnA9.addProductQnAReply(ProductQnAFixture.createProductQnAReply(i, productQnA9));
            productQnA10.addProductQnAReply(ProductQnAFixture.createProductQnAReply(i, productQnA10));
        }

        List<ProductOption> options = productList.stream()
                                                .flatMap(product -> product.getProductOptionSet().stream())
                                                .toList();
        firstProductOptions = new ArrayList<>(productList.get(0).getProductOptionSet());
        productList.forEach(v -> v.getProductOptionSet().clear());


        classificationRepository.saveAll(classifications);
        productRepository.saveAll(productList);
        productOptionRepository.saveAll(options);
        memberRepository.saveAll(memberList);
        productLikeRepository.save(productLike);
        productReviewRepository.saveAll(reviewList);
        productReviewReplyRepository.saveAll(reviewReplies);
        productQnARepository.saveAll(productQnAList);
    }

    @Test
    @DisplayName("상품 리뷰 조회")
    void getDetailReview() {
        ProductDetailPageDTO pageDTO = new ProductDetailPageDTO();
        String productId = productList.get(0).getId();
        List<ProductReviewResponseDTO> fixture = ProductReviewFixture.productReviewResponseDTOFilter(productId, reviewList, reviewReplies);
        ProductPageableDTO<ProductReviewResponseDTO> result = Assertions.assertDoesNotThrow(() -> productService.getDetailReview(pageDTO, productId));
        int reviewTotalElements = reviewList.stream().filter(v -> v.getProduct().getId() == productId).toList().size();

        Assertions.assertFalse(result.content().isEmpty());
        Assertions.assertEquals(fixture.size(), result.content().size());
        Assertions.assertEquals(reviewTotalElements, result.totalElements());

        for(int i = 0; i < fixture.size(); i++) {
            ProductReviewResponseDTO fixtureDTO = fixture.get(i);
            ProductReviewResponseDTO resultDTO = result.content().get(i);

            Assertions.assertEquals(fixtureDTO.reviewWriter(), resultDTO.reviewWriter());
            Assertions.assertEquals(fixtureDTO.reviewContent(), resultDTO.reviewContent());
            Assertions.assertEquals(fixtureDTO.answerContent(), resultDTO.answerContent());
        }
    }

    @Test
    @DisplayName("상품 리뷰가 없는 경우")
    void getEmptyDetailReview() {
        ProductDetailPageDTO pageDTO = new ProductDetailPageDTO();
        String productId = productList.get(2).getId();
        ProductPageableDTO<ProductReviewResponseDTO> result = Assertions.assertDoesNotThrow(() -> productService.getDetailReview(pageDTO, productId));

        Assertions.assertTrue(result.content().isEmpty());
        Assertions.assertEquals(0, result.totalElements());
        Assertions.assertEquals(0, result.totalPages());
        Assertions.assertTrue(result.empty());
    }

    @Test
    @DisplayName("상품 문의 조회")
    void getDetailQnA() {
        ProductDetailPageDTO pageDTO = new ProductDetailPageDTO();
        String productId = productList.get(0).getId();
        List<ProductQnA> qnaList = productQnAList.stream().filter(v -> v.getProduct().getId() == productId).sorted((v1, v2) -> Long.compare(v2.getId(), v1.getId())).toList();
        List<ProductQnAResponseDTO> fixture = ProductQnAFixture.createProductQnAResponseDTOList(qnaList);
        ProductPageableDTO<ProductQnAResponseDTO> result = Assertions.assertDoesNotThrow(() -> productService.getDetailQnA(pageDTO, productId));

        Assertions.assertFalse(result.content().isEmpty());
        Assertions.assertEquals(fixture.size(), result.content().size());
        Assertions.assertEquals(qnaList.size(), result.totalElements());
        Assertions.assertFalse(result.empty());

        for(int i = 0; i < fixture.size(); i++) {
            ProductQnAResponseDTO fixtureDTO = fixture.get(i);
            ProductQnAResponseDTO resultDTO = result.content().get(i);

            Assertions.assertEquals(fixtureDTO.qnaId(), resultDTO.qnaId());
            Assertions.assertEquals(fixtureDTO.qnaContent(), resultDTO.qnaContent());
            Assertions.assertEquals(fixtureDTO.writer(), resultDTO.writer());
            Assertions.assertEquals(fixtureDTO.replyList().size(), resultDTO.replyList().size());

            for(int j = 0; j < fixtureDTO.replyList().size(); j++) {
                ProductQnAReplyDTO fixtureReplies = fixtureDTO.replyList().get(j);
                ProductQnAReplyDTO resultReplies = resultDTO.replyList().get(j);

                Assertions.assertEquals(fixtureReplies.writer(), resultReplies.writer());
                Assertions.assertEquals(fixtureReplies.content(), resultReplies.content());
            }
        }
    }

    @Test
    @DisplayName("상품 문의가 없는 경우")
    void getEmptyDetailQnA() {
        ProductDetailPageDTO pageDTO = new ProductDetailPageDTO();
        String productId = productList.get(2).getId();
        ProductPageableDTO<ProductQnAResponseDTO> result = Assertions.assertDoesNotThrow(() -> productService.getDetailQnA(pageDTO, productId));

        Assertions.assertTrue(result.empty());
        Assertions.assertTrue(result.content().isEmpty());
        Assertions.assertEquals(0, result.totalElements());
        Assertions.assertEquals(0, result.totalPages());
    }

    @Test
    @DisplayName("상품 상세 정보 조회")
    void getProductDetail() {
        Product product = productList.get(0);
        String userId = memberList.get(0).getUserId();
        List<ProductOptionDTO> optionDTOFixture = firstProductOptions
                                                        .stream()
                                                        .map(v ->
                                                                new ProductOptionDTO(v.getId(), v.getSize(), v.getColor(), v.getStock())
                                                        )
                                                        .toList();
        List<String> thumbnailFixture = product.getProductThumbnailSet()
                                            .stream()
                                            .map(ProductThumbnail::getImageName)
                                            .toList();
        List<String> infoImageFixture = product.getProductInfoImageSet()
                                            .stream()
                                            .map(ProductInfoImage::getImageName)
                                            .toList();
        List<ProductReviewResponseDTO> reviewFixture = ProductReviewFixture.productReviewResponseDTOFilter(product.getId(), reviewList, reviewReplies);
        int reviewTotalElements = reviewList.stream()
                                            .filter(v ->
                                                    v.getProduct().getId() == product.getId()
                                            )
                                            .toList()
                                            .size();
        List<ProductQnA> qnaList = productQnAList.stream()
                                                .filter(v ->
                                                        v.getProduct().getId() == product.getId()
                                                )
                                                .sorted((v1, v2) ->
                                                        Long.compare(v2.getId(), v1.getId())
                                                )
                                                .toList();
        List<ProductQnAResponseDTO> qnaFixture = ProductQnAFixture.createProductQnAResponseDTOList(qnaList);

        ProductDetailDTO result = Assertions.assertDoesNotThrow(() -> productService.getProductDetail(product.getId(), userId));

        Assertions.assertNotNull(result);
        Assertions.assertEquals(product.getId(), result.productId());
        Assertions.assertEquals(product.getProductName(), result.productName());
        Assertions.assertEquals(product.getProductPrice(), result.productPrice());
        Assertions.assertEquals(product.getThumbnail(), result.productImageName());
        Assertions.assertTrue(result.likeStat());

        //option check
        Assertions.assertEquals(optionDTOFixture.size(), result.productOptionList().size());
        for(int i = 0; i < optionDTOFixture.size(); i++) {
            ProductOptionDTO fixtureDTO = optionDTOFixture.get(i);
            ProductOptionDTO resultDTO = result.productOptionList().get(i);

            Assertions.assertEquals(fixtureDTO.optionId(), resultDTO.optionId());
            Assertions.assertEquals(fixtureDTO.size(), resultDTO.size());
            Assertions.assertEquals(fixtureDTO.color(), resultDTO.color());
            Assertions.assertEquals(fixtureDTO.stock(), resultDTO.stock());
        }

        //thumbnail check
        Assertions.assertEquals(thumbnailFixture.size(), result.productThumbnailList().size());
        for(int i = 0; i < thumbnailFixture.size(); i++)
            Assertions.assertEquals(thumbnailFixture.get(i), result.productThumbnailList().get(i));

        //infoImage check
        Assertions.assertEquals(infoImageFixture.size(), result.productInfoImageList().size());
        for(int i = 0; i < infoImageFixture.size(); i++)
            Assertions.assertEquals(infoImageFixture.get(i), result.productInfoImageList().get(i));

        //review check
        Assertions.assertFalse(result.productReviewList().content().isEmpty());
        Assertions.assertEquals(reviewFixture.size(), result.productReviewList().content().size());
        Assertions.assertEquals(reviewTotalElements, result.productReviewList().totalElements());

        for(int i = 0; i < reviewFixture.size(); i++) {
            ProductReviewResponseDTO fixtureDTO = reviewFixture.get(i);
            ProductReviewResponseDTO resultDTO = result.productReviewList().content().get(i);

            Assertions.assertEquals(fixtureDTO.reviewWriter(), resultDTO.reviewWriter());
            Assertions.assertEquals(fixtureDTO.reviewContent(), resultDTO.reviewContent());
            Assertions.assertEquals(fixtureDTO.answerContent(), resultDTO.answerContent());
        }

        //qna check
        Assertions.assertFalse(result.productQnAList().content().isEmpty());
        Assertions.assertEquals(qnaFixture.size(), result.productQnAList().content().size());
        Assertions.assertEquals(qnaList.size(), result.productQnAList().totalElements());
        Assertions.assertFalse(result.productQnAList().empty());

        for(int i = 0; i < qnaFixture.size(); i++) {
            ProductQnAResponseDTO fixtureDTO = qnaFixture.get(i);
            ProductQnAResponseDTO resultDTO = result.productQnAList().content().get(i);

            Assertions.assertEquals(fixtureDTO.qnaId(), resultDTO.qnaId());
            Assertions.assertEquals(fixtureDTO.qnaContent(), resultDTO.qnaContent());
            Assertions.assertEquals(fixtureDTO.writer(), resultDTO.writer());
            Assertions.assertEquals(fixtureDTO.replyList().size(), resultDTO.replyList().size());

            for(int j = 0; j < fixtureDTO.replyList().size(); j++) {
                ProductQnAReplyDTO fixtureReplies = fixtureDTO.replyList().get(j);
                ProductQnAReplyDTO resultReplies = resultDTO.replyList().get(j);

                Assertions.assertEquals(fixtureReplies.writer(), resultReplies.writer());
                Assertions.assertEquals(fixtureReplies.content(), resultReplies.content());
            }
        }
    }

    @Test
    @DisplayName("잘못된 상품 정보를 조회하는 경우")
    void getProductDetailThrow() {
        String productId = "FailProduct";
        String userId = memberList.get(0).getUserId();

        Assertions.assertThrows(CustomNotFoundException.class, () -> productService.getProductDetail(productId, userId));
    }

    @Test
    @DisplayName("관심상품 등록")
    void likeProduct() {
        String productId = productList.get(1).getId();
        String userId = memberList.get(1).getUserId();

        String result = Assertions.assertDoesNotThrow(() -> productService.likeProduct(productId, userId));
        Assertions.assertEquals(Result.OK.getResultKey(), result);

        boolean isLike = productLikeRepository.countByUserIdAndProductId(productId, userId) == 1;
        Assertions.assertTrue(isLike);
    }

    @Test
    @DisplayName("관심 상품 등록 시 상품 아이디가 유효하지 않은 경우")
    void likeProductNotFoundProduct() {
        String productId = "WrongProductId";
        String userId = memberList.get(1).getUserId();

        Assertions.assertThrows(CustomNotFoundException.class, () -> productService.likeProduct(productId, userId));
    }

    @Test
    @DisplayName("관심 상품 등록 해제")
    void deLikeProduct() {
        String productId = productList.get(1).getId();
        String userId = memberList.get(1).getUserId();
        productService.likeProduct(productId, userId);

        String result = Assertions.assertDoesNotThrow(() -> productService.deLikeProduct(productId, userId));
        Assertions.assertEquals(Result.OK.getResultKey(), result);

        boolean isLike = productLikeRepository.countByUserIdAndProductId(productId, userId) == 0;
        Assertions.assertTrue(isLike);
    }

    @Test
    @DisplayName("관심 상품 등록 해제 시 상품 아이디가 유효하지 않은 경우")
    void deLikeProductNotFoundProduct() {
        String productId = "WrongProductId";
        String userId = memberList.get(1).getUserId();

        Assertions.assertThrows(CustomNotFoundException.class, () -> productService.deLikeProduct(productId, userId));
    }

    @Test
    @DisplayName("상품 문의 작성")
    void postProductQnA() {
        String productId = productList.get(2).getId();
        Member member = memberList.get(1);
        String content = "post content";
        ProductQnAPostDTO postDTO = new ProductQnAPostDTO(productId, content);
        Pageable pageable = ProductQnAFixture.createQnAPageable();
        String saveResult = Assertions.assertDoesNotThrow(() -> productService.postProductQnA(postDTO, member.getUserId()));
        Page<ProductQnADTO> result = productQnARepository.findByProductId(productId, pageable);

        Assertions.assertEquals(Result.OK.getResultKey(), saveResult);
        Assertions.assertEquals(member.getNickname() == null ? member.getUserName() : member.getNickname(), result.getContent().get(0).writer());
        Assertions.assertEquals(content, result.getContent().get(0).qnaContent());
    }
}
