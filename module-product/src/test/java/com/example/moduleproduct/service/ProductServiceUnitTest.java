package com.example.moduleproduct.service;

import com.example.moduleauth.repository.MemberRepository;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleconfig.config.exception.customException.CustomNotFoundException;
import com.example.moduleproduct.ModuleProductApplication;
import com.example.moduleproduct.fixture.*;
import com.example.moduleproduct.model.dto.page.ProductDetailPageDTO;
import com.example.moduleproduct.model.dto.product.business.ProductOptionDTO;
import com.example.moduleproduct.model.dto.product.business.ProductQnADTO;
import com.example.moduleproduct.model.dto.product.business.ProductQnAResponseDTO;
import com.example.moduleproduct.model.dto.product.business.ProductReviewResponseDTO;
import com.example.moduleproduct.model.dto.product.in.ProductQnAPostDTO;
import com.example.moduleproduct.model.dto.product.out.ProductDetailDTO;
import com.example.moduleproduct.model.dto.product.out.ProductPageableDTO;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productInfoImage.ProductInfoImageRepository;
import com.example.moduleproduct.repository.productLike.ProductLikeRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import com.example.moduleproduct.repository.productQnA.ProductQnARepository;
import com.example.moduleproduct.repository.productQnAReply.ProductQnAReplyRepository;
import com.example.moduleproduct.repository.productReview.ProductReviewRepository;
import com.example.moduleproduct.repository.productThumbnail.ProductThumbnailRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = ModuleProductApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
public class ProductServiceUnitTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductOptionRepository productOptionRepository;

    @Mock
    private ProductLikeRepository productLikeRepository;

    @Mock
    private ProductThumbnailRepository productThumbnailRepository;

    @Mock
    private ProductInfoImageRepository productInfoImageRepository;

    @Mock
    private ProductReviewRepository productReviewRepository;

    @Mock
    private ProductQnARepository productQnARepository;

    @Mock
    private ProductQnAReplyRepository productQnAReplyRepository;

    @Mock
    private MemberRepository memberRepository;


    @Test
    @DisplayName("상품 상세 페이지 Review 조회")
    void getDetailReview() {
        Page<ProductReviewResponseDTO> reviewResponse = ProductReviewFixture.createPageObjectByProductReview();
        Pageable reviewPageable = ProductReviewFixture.createReviewPageable();
        ProductDetailPageDTO pageDTO = new ProductDetailPageDTO();

        when(productReviewRepository.findByProductId("testProduct", reviewPageable))
                .thenReturn(new PageImpl<>(reviewResponse.getContent(), reviewPageable, reviewResponse.getContent().size()));

        ProductPageableDTO<ProductReviewResponseDTO> result = productService.getDetailReview(pageDTO, "testProduct");

        Assertions.assertEquals(reviewResponse.getContent(), result.content());
        Assertions.assertEquals(reviewResponse.getTotalElements(), result.totalElements());
    }

    @Test
    @DisplayName("상품 상세 페이지 Review가 하나도 없는 경우")
    void getEmptyDetailReview() {
        Pageable reviewPageable = ProductReviewFixture.createReviewPageable();
        ProductDetailPageDTO pageDTO = new ProductDetailPageDTO();

        when(productReviewRepository.findByProductId("testProduct", reviewPageable))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        ProductPageableDTO<ProductReviewResponseDTO> result = productService.getDetailReview(pageDTO, "testProduct");

        Assertions.assertNotNull(result);
        Assertions.assertEquals(Collections.emptyList(), result.content());
        Assertions.assertTrue(result.empty());
        Assertions.assertEquals(0, result.totalElements());
    }

    @Test
    @DisplayName("상품 상세 페이지 QnA 조회")
    void getDetailQnA() {
        Page<ProductQnADTO> qnaResponse = ProductQnAFixture.createPageObjectByProductQnA();
        List<ProductQnAReply> replyList = ProductQnAFixture.createProductQnAReplyList();
        Pageable pageable = ProductQnAFixture.createQnAPageable();
        ProductDetailPageDTO pageDTO = new ProductDetailPageDTO();
        when(productQnARepository.findByProductId("testProduct", pageable)).thenReturn(qnaResponse);
        when(productQnAReplyRepository.findByQnAReply(anyList())).thenReturn(replyList);

        ProductPageableDTO<ProductQnAResponseDTO> result = productService.getDetailQnA(pageDTO, "testProduct");
        int totalResultReply = result.content()
                                    .stream()
                                    .filter(v -> !v.replyList().isEmpty())
                                    .mapToInt(v -> v.replyList().size())
                                    .sum();

        Assertions.assertEquals(qnaResponse.getContent().size(), result.content().size());
        Assertions.assertEquals(qnaResponse.getTotalElements(), result.totalElements());
        Assertions.assertEquals(replyList.size(), totalResultReply);
    }

    @Test
    @DisplayName("상품 상세 페이지 QnA가 없는 경우")
    void getEmptyDetailQnA() {
        Pageable pageable = ProductQnAFixture.createQnAPageable();
        ProductDetailPageDTO pageDTO = new ProductDetailPageDTO();
        when(productQnARepository.findByProductId("testProduct", pageable)).thenReturn(new PageImpl<>(Collections.emptyList()));

        ProductPageableDTO<ProductQnAResponseDTO> result = productService.getDetailQnA(pageDTO, "testProduct");
        int totalResultReply = result.content()
                                    .stream()
                                    .filter(v -> !v.replyList().isEmpty())
                                    .mapToInt(v -> v.replyList().size())
                                    .sum();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(Collections.emptyList(), result.content());
        Assertions.assertEquals(0, result.totalElements());
        Assertions.assertEquals(0, totalResultReply);
    }

    @Test
    @DisplayName("상품 상세 페이지 조회")
    void getProductDetail() {
        Product product = ProductFixture.createOneProductEntity();
        String userId = MemberFixture.createMember().getUserId();
        List<ProductOptionDTO> optionDTOList = ProductFixture.createProductOptionDTOList();
        List<String> thumbnailList = ProductFixture.createProductThumbnailList().stream().map(ProductThumbnail::getImageName).toList();
        List<String> infoImageList = ProductFixture.createProductInfoImageList().stream().map(ProductInfoImage::getImageName).toList();
        Page<ProductReviewResponseDTO> reviewResponse = ProductReviewFixture.createPageObjectByProductReview();
        Pageable reviewPageable = ProductReviewFixture.createReviewPageable();
        Page<ProductQnADTO> qnaResponse = ProductQnAFixture.createPageObjectByProductQnA();
        Pageable qnaPageable = ProductQnAFixture.createQnAPageable();
        List<ProductQnAReply> qnaReplyList = ProductQnAFixture.createProductQnAReplyList();

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(productLikeRepository.countByUserIdAndProductId(product.getId(), userId)).thenReturn(1);
        when(productOptionRepository.findByDetailOption(product.getId())).thenReturn(optionDTOList);
        when(productThumbnailRepository.findByProductId(product.getId())).thenReturn(thumbnailList);
        when(productInfoImageRepository.findByProductId(product.getId())).thenReturn(infoImageList);
        when(productReviewRepository.findByProductId(product.getId(), reviewPageable)).thenReturn(new PageImpl<>(reviewResponse.getContent(), reviewPageable, reviewResponse.getContent().size()));
        when(productQnARepository.findByProductId(product.getId(), qnaPageable)).thenReturn(qnaResponse);
        when(productQnAReplyRepository.findByQnAReply(anyList())).thenReturn(qnaReplyList);

        ProductDetailDTO result = productService.getProductDetail(product.getId(), userId);
        int totalResultReply = result.productQnAList()
                                    .content()
                                    .stream()
                                    .filter(v -> !v.replyList().isEmpty())
                                    .mapToInt(v -> v.replyList().size())
                                    .sum();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(product.getId(), result.productId());
        Assertions.assertEquals(product.getProductDiscount(), result.discount());
        Assertions.assertEquals(product.getProductPrice(), result.productPrice());
        Assertions.assertTrue(result.likeStat());
        Assertions.assertEquals(optionDTOList, result.productOptionList());
        Assertions.assertEquals(thumbnailList, result.productThumbnailList());
        Assertions.assertEquals(infoImageList, result.productInfoImageList());
        Assertions.assertEquals(reviewResponse.getContent(), result.productReviewList().content());
        Assertions.assertEquals(reviewResponse.getTotalElements(), result.productReviewList().totalElements());
        Assertions.assertEquals(qnaResponse.getContent().size(), result.productQnAList().content().size());
        Assertions.assertEquals(qnaResponse.getTotalElements(), result.productQnAList().totalElements());
        Assertions.assertEquals(qnaReplyList.size(), totalResultReply);
    }

    @Test
    @DisplayName("존재하지 않는 상품 아이디를 요청 받은 경우")
    void getProductDetailNotFound() {
        when(productRepository.findById("testProduct")).thenReturn(Optional.empty());

        Assertions.assertThrows(CustomNotFoundException.class,
                                () -> productService.getProductDetail("testProduct", "testUser")
                        );
    }

    @Test
    @DisplayName("상품 상세 페이지 조회 시 리뷰와 문의 데이터가 존재하지 않는 경우")
    void getProductDetailForEmptyReviewAndQnA() {
        Product product = ProductFixture.createOneProductEntity();
        String userId = MemberFixture.createMember().getUserId();
        List<ProductOptionDTO> optionDTOList = ProductFixture.createProductOptionDTOList();
        List<String> thumbnailList = ProductFixture.createProductThumbnailList().stream().map(ProductThumbnail::getImageName).toList();
        List<String> infoImageList = ProductFixture.createProductInfoImageList().stream().map(ProductInfoImage::getImageName).toList();
        Pageable reviewPageable = ProductReviewFixture.createReviewPageable();
        Pageable qnaPageable = ProductQnAFixture.createQnAPageable();

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(productLikeRepository.countByUserIdAndProductId(product.getId(), userId)).thenReturn(1);
        when(productOptionRepository.findByDetailOption(product.getId())).thenReturn(optionDTOList);
        when(productThumbnailRepository.findByProductId(product.getId())).thenReturn(thumbnailList);
        when(productInfoImageRepository.findByProductId(product.getId())).thenReturn(infoImageList);
        when(productReviewRepository.findByProductId(product.getId(), reviewPageable)).thenReturn(new PageImpl<>(Collections.emptyList()));
        when(productQnARepository.findByProductId(product.getId(), qnaPageable)).thenReturn(new PageImpl<>(Collections.emptyList()));
        when(productQnAReplyRepository.findByQnAReply(anyList())).thenReturn(Collections.emptyList());

        ProductDetailDTO result = productService.getProductDetail(product.getId(), userId);
        int totalResultReply = result.productQnAList()
                                    .content()
                                    .stream()
                                    .filter(v -> !v.replyList().isEmpty())
                                    .mapToInt(v -> v.replyList().size())
                                    .sum();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(product.getId(), result.productId());
        Assertions.assertEquals(product.getProductDiscount(), result.discount());
        Assertions.assertEquals(product.getProductPrice(), result.productPrice());
        Assertions.assertTrue(result.likeStat());
        Assertions.assertEquals(optionDTOList, result.productOptionList());
        Assertions.assertEquals(thumbnailList, result.productThumbnailList());
        Assertions.assertEquals(infoImageList, result.productInfoImageList());
        Assertions.assertEquals(Collections.emptyList(), result.productReviewList().content());
        Assertions.assertEquals(0, result.productReviewList().totalElements());
        Assertions.assertEquals(Collections.emptyList(), result.productQnAList().content());
        Assertions.assertEquals(0, result.productQnAList().totalElements());
        Assertions.assertEquals(0, totalResultReply);
    }

    @Test
    @DisplayName("상품 상세 페이지 조회 시 썸네일과 정보 이미지가 존재하지 않는 경우")
    void getProductDetailForEmptyImages() {
        Product product = ProductFixture.createOneProductEntity();
        String userId = MemberFixture.createMember().getUserId();
        List<ProductOptionDTO> optionDTOList = ProductFixture.createProductOptionDTOList();
        Page<ProductReviewResponseDTO> reviewResponse = ProductReviewFixture.createPageObjectByProductReview();
        Pageable reviewPageable = ProductReviewFixture.createReviewPageable();
        Page<ProductQnADTO> qnaResponse = ProductQnAFixture.createPageObjectByProductQnA();
        Pageable qnaPageable = ProductQnAFixture.createQnAPageable();
        List<ProductQnAReply> qnaReplyList = ProductQnAFixture.createProductQnAReplyList();

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(productLikeRepository.countByUserIdAndProductId(product.getId(), userId)).thenReturn(1);
        when(productOptionRepository.findByDetailOption(product.getId())).thenReturn(optionDTOList);
        when(productThumbnailRepository.findByProductId(product.getId())).thenReturn(Collections.emptyList());
        when(productInfoImageRepository.findByProductId(product.getId())).thenReturn(Collections.emptyList());
        when(productReviewRepository.findByProductId(product.getId(), reviewPageable)).thenReturn(new PageImpl<>(reviewResponse.getContent(), reviewPageable, reviewResponse.getContent().size()));
        when(productQnARepository.findByProductId(product.getId(), qnaPageable)).thenReturn(qnaResponse);
        when(productQnAReplyRepository.findByQnAReply(anyList())).thenReturn(qnaReplyList);

        ProductDetailDTO result = productService.getProductDetail(product.getId(), userId);
        int totalResultReply = result.productQnAList()
                .content()
                .stream()
                .filter(v -> !v.replyList().isEmpty())
                .mapToInt(v -> v.replyList().size())
                .sum();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(product.getId(), result.productId());
        Assertions.assertEquals(product.getProductDiscount(), result.discount());
        Assertions.assertEquals(product.getProductPrice(), result.productPrice());
        Assertions.assertTrue(result.likeStat());
        Assertions.assertEquals(optionDTOList, result.productOptionList());
        Assertions.assertEquals(Collections.emptyList(), result.productThumbnailList());
        Assertions.assertEquals(Collections.emptyList(), result.productInfoImageList());
        Assertions.assertEquals(reviewResponse.getContent(), result.productReviewList().content());
        Assertions.assertEquals(reviewResponse.getTotalElements(), result.productReviewList().totalElements());
        Assertions.assertEquals(qnaResponse.getContent().size(), result.productQnAList().content().size());
        Assertions.assertEquals(qnaResponse.getTotalElements(), result.productQnAList().totalElements());
        Assertions.assertEquals(qnaReplyList.size(), totalResultReply);
    }

    @Test
    @DisplayName("관심 상품으로 등록")
    void likeProduct() {
        ProductLike productLike = ProductLikeFixture.createSuccessLikeCountEntity();
        Member member = productLike.getMember();
        Product product = productLike.getProduct();

        when(memberRepository.findById(member.getUserId())).thenReturn(Optional.of(member));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(productLikeRepository.save(productLike)).thenReturn(productLike);

        String result = productService.likeProduct(product.getId(), member.getUserId());

        Assertions.assertEquals(Result.OK.getResultKey(), result);
    }

    @Test
    @DisplayName("관심상품 등록 시 잘못된 상품 정보")
    void likeProductToWrongProduct() {
        Member member = MemberFixture.createMember();
        String productId = "testProduct";
        when(memberRepository.findById(member.getUserId())).thenReturn(Optional.of(member));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        Assertions.assertThrows(CustomNotFoundException.class, () -> productService.likeProduct(productId, member.getUserId()));
    }

    @Test
    @DisplayName("관심 상품 제거")
    void deLikeProduct() {
        ProductLike productLike = ProductLikeFixture.createSuccessLikeCountEntity();
        Member member = productLike.getMember();
        Product product = productLike.getProduct();

        when(memberRepository.findById(member.getUserId())).thenReturn(Optional.of(member));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        doNothing().when(productLikeRepository).deleteByUserIdAndProductId(productLike);

        String result = productService.deLikeProduct(product.getId(), member.getUserId());

        Assertions.assertEquals(Result.OK.getResultKey(), result);
    }

    @Test
    @DisplayName("상품 문의 작성")
    void postProductQnA() {
        Member member = MemberFixture.createMember();
        Product product = ProductFixture.createOneProductEntity();
        ProductQnAPostDTO postDTO = new ProductQnAPostDTO(product.getId(), "testContent");
        ProductQnA productQnA = postDTO.toProductQnAEntity(member, product);

        when(memberRepository.findById(member.getUserId())).thenReturn(Optional.of(member));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(productQnARepository.save(productQnA)).thenReturn(productQnA);

        String result = productService.postProductQnA(postDTO, member.getUserId());

        Assertions.assertEquals(Result.OK.getResultKey(), result);
    }

    @Test
    @DisplayName("상품 문의 작성 시 잘못된 상품 아이디")
    void postProductQnAToWrongProduct() {
        Member member = MemberFixture.createMember();
        String productId = "testProduct";
        ProductQnAPostDTO postDTO = new ProductQnAPostDTO(productId, "testContent");

        when(memberRepository.findById(member.getUserId())).thenReturn(Optional.of(member));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        Assertions.assertThrows(CustomNotFoundException.class, () -> productService.postProductQnA(postDTO, member.getUserId()));
    }
}
