package com.example.moduleapi.controller.product;

import com.example.moduleapi.ModuleApiApplication;
import com.example.moduleapi.config.exception.ExceptionEntity;
import com.example.moduleapi.config.exception.ValidationError;
import com.example.moduleapi.config.exception.ValidationExceptionEntity;
import com.example.moduleapi.fixture.TokenFixture;
import com.example.moduleapi.model.response.PagingElementsResponseDTO;
import com.example.modulecommon.fixture.*;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.utils.TestPaginationUtils;
import com.example.modulecommon.utils.ProductDiscountUtils;
import com.example.moduleconfig.properties.CookieProperties;
import com.example.moduleconfig.properties.TokenProperties;
import com.example.moduleproduct.model.dto.page.ProductDetailPageDTO;
import com.example.moduleproduct.model.dto.product.business.ProductOptionDTO;
import com.example.moduleproduct.model.dto.product.business.ProductQnAResponseDTO;
import com.example.moduleproduct.model.dto.product.in.ProductQnAPostDTO;
import com.example.moduleproduct.model.dto.product.out.ProductDetailDTO;
import com.example.moduleproduct.model.dto.product.out.ProductDetailReviewDTO;
import com.example.moduleproduct.model.dto.product.out.ProductPageableDTO;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productInfoImage.ProductInfoImageRepository;
import com.example.moduleproduct.repository.productLike.ProductLikeRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import com.example.moduleproduct.repository.productQnA.ProductQnARepository;
import com.example.moduleproduct.repository.productReview.ProductReviewRepository;
import com.example.moduleproduct.repository.productThumbnail.ProductThumbnailRepository;
import com.example.moduleuser.repository.AuthRepository;
import com.example.moduleuser.repository.MemberRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.method.annotation.HandlerMethodValidationException;


import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest(classes = ModuleApiApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class ProductControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private EntityManager em;

    @Autowired
    private TokenFixture tokenFixture;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthRepository authRepository;

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
    private ProductReviewRepository productReviewRepository;

    @Autowired
    private ProductQnARepository productQnARepository;

    @Autowired
    private ProductLikeRepository productLikeRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private TokenProperties tokenProperties;

    @Autowired
    private CookieProperties cookieProperties;

    private Map<String, String> tokenMap;

    private String accessTokenValue;

    private String refreshTokenValue;

    private String inoValue;

    private Member member;

    private Product product;

    private List<ProductReview> allProductReview;

    private List<ProductQnA> allProductQnA;

    private static final String URL_PREFIX = "/api/product/";

    @BeforeEach
    void init() {
        MemberAndAuthFixtureDTO memberAndAuthFixtureDTO = MemberAndAuthFixture.createDefaultMember(10);
        MemberAndAuthFixtureDTO anonymousFixtureDTO = MemberAndAuthFixture.createAnonymous();
        List<Member> memberList = memberAndAuthFixtureDTO.memberList();
        List<Member> saveMemberList = new ArrayList<>(memberList);
        saveMemberList.addAll(anonymousFixtureDTO.memberList());
        List<Auth> saveAuthList = new ArrayList<>(memberAndAuthFixtureDTO.authList());
        saveAuthList.addAll(anonymousFixtureDTO.authList());
        memberRepository.saveAll(saveMemberList);
        authRepository.saveAll(saveAuthList);
        member = memberList.get(0);

        tokenMap = tokenFixture.createAndSaveAllToken(member);
        accessTokenValue = tokenMap.get(tokenProperties.getAccess().getHeader());
        refreshTokenValue = tokenMap.get(tokenProperties.getRefresh().getHeader());
        inoValue = tokenMap.get(cookieProperties.getIno().getHeader());

        List<Classification> classificationList = ClassificationFixture.createClassifications();
        classificationRepository.saveAll(classificationList);

        List<Product> productList = ProductFixture.createProductFixtureList(10, classificationList.get(0));
        List<ProductOption> productOptionList = productList.stream()
                .flatMap(v ->
                        v.getProductOptions().stream()
                )
                .toList();
        List<ProductThumbnail> productThumbnailList = productList.stream()
                .flatMap(v ->
                        v.getProductThumbnails().stream()
                )
                .toList();
        List<ProductInfoImage> productInfoImageList = productList.stream()
                .flatMap(v ->
                        v.getProductInfoImages().stream()
                )
                .toList();
        productRepository.saveAll(productList);
        productOptionRepository.saveAll(productOptionList);
        productThumbnailRepository.saveAll(productThumbnailList);
        productInfoImageRepository.saveAll(productInfoImageList);

        product = productList.get(0);

        allProductReview = ProductReviewFixture.createDefaultReview(memberList, productOptionList);
        productReviewRepository.saveAll(allProductReview);

        allProductQnA = ProductQnAFixture.createDefaultProductQnA(memberList, productList);
        productQnARepository.saveAll(allProductQnA);

        List<ProductLike> productLikeList = ProductLikeFixture.createDefaultProductLike(memberList, productList);
        productLikeRepository.saveAll(productLikeList);

        em.flush();
        em.clear();
    }

    @AfterEach
    void cleanUP() {
        String accessKey = tokenMap.get("accessKey");
        String refreshKey = tokenMap.get("refreshKey");

        redisTemplate.delete(accessKey);
        redisTemplate.delete(refreshKey);
        redisTemplate.delete(member.getUserId());
    }

    private ProductDetailDTO getProductDetailDTOFixture(boolean likeStatus) {
        List<ProductOptionDTO> productOptionListFixture = product.getProductOptions()
                .stream()
                .filter(ProductOption::isOpen)
                .map(v -> new ProductOptionDTO(
                        v.getId(),
                        v.getSize(),
                        v.getColor(),
                        v.getStock()
                ))
                .toList();
        List<String> thumbnailListFixture = product.getProductThumbnails()
                .stream()
                .map(ProductThumbnail::getImageName)
                .toList();
        List<String> infoImageListFixture = product.getProductInfoImages()
                .stream()
                .map(ProductInfoImage::getImageName)
                .toList();
        ProductDetailPageDTO pageDTO = new ProductDetailPageDTO(1);
        List<ProductDetailReviewDTO> reviewDTOFixtureList = allProductReview.stream()
                .filter(v ->
                        v.getProduct().getId().equals(product.getId())
                )
                .sorted(
                        Comparator.comparingLong(ProductReview::getId)
                                .reversed()
                )
                .map(v -> new ProductDetailReviewDTO(
                        v.getMember().getNickname(),
                        v.getReviewContent(),
                        v.getCreatedAt(),
                        null,
                        null
                ))
                .toList();
        List<ProductDetailReviewDTO> reviewDTOContentFixture = reviewDTOFixtureList.stream()
                .limit(pageDTO.reviewAmount())
                .toList();
        ProductPageableDTO<ProductDetailReviewDTO> reviewFixture = new ProductPageableDTO<>(
                reviewDTOContentFixture,
                reviewDTOFixtureList.isEmpty(),
                1,
                TestPaginationUtils.getTotalPages(reviewDTOFixtureList.size(), pageDTO.reviewAmount()),
                reviewDTOFixtureList.size()
        );
        List<ProductQnAResponseDTO> productQnADTOFixtureList = allProductQnA.stream()
                .filter(v ->
                        v.getProduct().getId().equals(product.getId())
                )
                .sorted(
                        Comparator.comparingLong(ProductQnA::getId)
                                .reversed()
                )
                .map(v -> new ProductQnAResponseDTO(
                        v.getId(),
                        v.getMember().getNickname(),
                        v.getQnaContent(),
                        v.getCreatedAt().toLocalDate(),
                        v.isProductQnAStat(),
                        Collections.emptyList()
                ))
                .toList();
        List<ProductQnAResponseDTO> productQnADTOContentFixture = productQnADTOFixtureList.stream()
                .limit(pageDTO.qnaAmount())
                .toList();
        ProductPageableDTO<ProductQnAResponseDTO> qnaFixture = new ProductPageableDTO<>(
                productQnADTOContentFixture,
                productQnADTOFixtureList.isEmpty(),
                1,
                TestPaginationUtils.getTotalPages(productQnADTOFixtureList.size(), pageDTO.qnaAmount()),
                productQnADTOFixtureList.size()
        );

        return new ProductDetailDTO(
                product.getId(),
                product.getProductName(),
                product.getProductPrice(),
                product.getThumbnail(),
                likeStatus,
                product.getProductDiscount(),
                ProductDiscountUtils.calcDiscountPrice(product.getProductPrice(), product.getProductDiscount()),
                productOptionListFixture,
                thumbnailListFixture,
                infoImageListFixture,
                reviewFixture,
                qnaFixture
        );
    }

    private void validateProductDetailResult(ProductDetailDTO response, ProductDetailDTO responseFixture) {
        assertNotNull(response);

        assertEquals(responseFixture.productId(), response.productId());
        assertEquals(responseFixture.productName(), response.productName());
        assertEquals(responseFixture.productPrice(), response.productPrice());
        assertEquals(responseFixture.productImageName(), response.productImageName());
        assertEquals(responseFixture.likeStat(), response.likeStat());
        assertEquals(responseFixture.discount(), response.discount());
        assertEquals(responseFixture.discountPrice(), response.discountPrice());

        assertEquals(responseFixture.productOptionList().size(), response.productOptionList().size());
        response.productOptionList()
                .forEach(v ->
                        assertTrue(responseFixture.productOptionList().contains(v))
                );
        assertEquals(responseFixture.productThumbnailList().size(), response.productThumbnailList().size());
        response.productThumbnailList()
                .forEach(v ->
                        assertTrue(responseFixture.productThumbnailList().contains(v))
                );
        assertEquals(responseFixture.productInfoImageList().size(), response.productInfoImageList().size());
        response.productInfoImageList()
                .forEach(v ->
                        assertTrue(responseFixture.productInfoImageList().contains(v))
                );
        assertEquals(responseFixture.productReviewList().empty(), response.productReviewList().empty());
        assertEquals(responseFixture.productReviewList().totalElements(), response.productReviewList().totalElements());
        assertEquals(responseFixture.productReviewList().totalPages(), response.productReviewList().totalPages());
        assertEquals(responseFixture.productReviewList().content().size(), response.productReviewList().content().size());
        response.productReviewList().content()
                .forEach(v ->
                        assertTrue(responseFixture.productReviewList().content().contains(v))
                );

        assertEquals(responseFixture.productQnAList().empty(), response.productQnAList().empty());
        assertEquals(responseFixture.productQnAList().totalElements(), response.productQnAList().totalElements());
        assertEquals(responseFixture.productQnAList().totalPages(), response.productQnAList().totalPages());
        assertEquals(responseFixture.productQnAList().content().size(), response.productQnAList().content().size());
        response.productQnAList().content()
                .forEach(v ->
                        assertTrue(responseFixture.productQnAList().content().contains(v))
                );
    }

    @Test
    @DisplayName(value = "회원의 상품 상세 조회. 관심 상품인 경우")
    void getProductDetail() throws Exception {
        ProductDetailDTO responseFixture = getProductDetailDTOFixture(true);
        MvcResult result = mockMvc.perform(get(URL_PREFIX + product.getId())
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ProductDetailDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        validateProductDetailResult(responseFixture, response);
    }

    @Test
    @DisplayName(value = "회원의 상품 상세 조회. 관심 상품이 아닌 경우")
    void getProductDetailNotLike() throws Exception {
        productLikeRepository.deleteAll();
        ProductDetailDTO responseFixture = getProductDetailDTOFixture(false);
        MvcResult result = mockMvc.perform(get(URL_PREFIX + product.getId())
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ProductDetailDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        validateProductDetailResult(responseFixture, response);
    }

    @Test
    @DisplayName(value = "비회원의 상품 상세 조회.")
    void getProductDetailByAnonymous() throws Exception {
        ProductDetailDTO responseFixture = getProductDetailDTOFixture(false);
        MvcResult result = mockMvc.perform(get(URL_PREFIX + product.getId()))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ProductDetailDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        validateProductDetailResult(responseFixture, response);
    }

    @Test
    @DisplayName(value = "상품 상세 조회. 상품 아이디가 잘못 된 경우")
    void getProductDetailWrongProductId() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "noneProductId"))
                .andExpect(status().is(400))
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "상품의 리뷰 조회")
    void getProductReview() throws Exception {
        ProductDetailDTO responseFixture = getProductDetailDTOFixture(false);

        MvcResult result = mockMvc.perform(get(URL_PREFIX + product.getId() + "/review")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingElementsResponseDTO<ProductDetailReviewDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(responseFixture.productReviewList().empty(), response.content().isEmpty());
        assertEquals(responseFixture.productReviewList().totalElements(), response.totalElements());
        assertEquals(responseFixture.productReviewList().totalPages(), response.totalPages());
        assertEquals(responseFixture.productReviewList().content().size(), response.content().size());
        response.content()
                .forEach(v ->
                        assertTrue(responseFixture.productReviewList().content().contains(v))
                );
    }

    @Test
    @DisplayName(value = "상품의 리뷰 조회. 상품 아이디가 잘못 된 경우")
    void getProductReviewWrongProductId() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "noneProductId/review")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingElementsResponseDTO<ProductDetailReviewDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertEquals(0, response.totalElements());
        assertEquals(0, response.totalPages());
    }

    @Test
    @DisplayName(value = "상품의 리뷰 조회. 페이지값이 1보다 작은 경우")
    void getProductReviewValidationPageIsZero() throws Exception {
        ProductDetailDTO responseFixture = getProductDetailDTOFixture(false);

        MvcResult result = mockMvc.perform(get(URL_PREFIX + product.getId() + "/review")
                        .param("page", "0"))
                .andExpect(status().isBadRequest())
                .andReturn();

        Exception ex = result.getResolvedException();
        assertInstanceOf(HandlerMethodValidationException.class, ex);

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
        assertNull(response.errors());
    }

    @Test
    @DisplayName(value = "상품의 문의 조회")
    void getProductQnA() throws Exception {
        ProductDetailDTO responseFixture = getProductDetailDTOFixture(false);

        MvcResult result = mockMvc.perform(get(URL_PREFIX + product.getId() + "/qna")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingElementsResponseDTO<ProductQnAResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(responseFixture.productQnAList().empty(), response.content().isEmpty());
        assertEquals(responseFixture.productQnAList().totalElements(), response.totalElements());
        assertEquals(responseFixture.productQnAList().totalPages(), response.totalPages());
        assertEquals(responseFixture.productQnAList().content().size(), response.content().size());
        response.content()
                .forEach(v ->
                        assertTrue(responseFixture.productQnAList().content().contains(v))
                );
    }

    @Test
    @DisplayName(value = "상품의 문의 조회. 상품 아이디가 잘못 된 경우")
    void getProductQnAWrongProductId() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "noneProductId/qna")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingElementsResponseDTO<ProductQnAResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertEquals(0, response.totalElements());
        assertEquals(0, response.totalPages());
    }

    @Test
    @DisplayName(value = "상품의 문의 조회. 페이지 값이 1보다 작은 경우")
    void getProductQnAValidationPageIsZero() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + product.getId() + "/qna")
                        .param("page", "0"))
                .andExpect(status().isBadRequest())
                .andReturn();

        Exception ex = result.getResolvedException();
        assertInstanceOf(HandlerMethodValidationException.class, ex);

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "상품 문의 작성")
    void postProductQnA() throws Exception {
        productQnARepository.deleteAll();
        ProductQnAPostDTO postDTO = new ProductQnAPostDTO(product.getId(), "test insert Product QnA content");
        String requestDTO = om.writeValueAsString(postDTO);

        mockMvc.perform(post(URL_PREFIX + "qna")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isNoContent())
                .andReturn();

        List<ProductQnA> insertList = productQnARepository.findAll();
        assertNotNull(insertList);
        assertFalse(insertList.isEmpty());
        assertEquals(1, insertList.size());

        ProductQnA insertData = insertList.get(0);
        assertEquals(member.getUserId(), insertData.getMember().getUserId());
        assertEquals(postDTO.productId(), insertData.getProduct().getId());
        assertEquals(postDTO.content(), insertData.getQnaContent());
    }

    @Test
    @DisplayName(value = "상품 문의 작성. 상품 아이디가 잘못 된 경우")
    void postProductQnAWrongProductId() throws Exception {
        ProductQnAPostDTO postDTO = new ProductQnAPostDTO("noneProductId", "test insert Product QnA content");
        String requestDTO = om.writeValueAsString(postDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().is(400))
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "상품 문의 작성. 상품 아이디가 null인 경우")
    void postProductQnAValidationProductIdIsNull() throws Exception {
        ProductQnAPostDTO postDTO = new ProductQnAPostDTO(null, "test insert Product QnA content");
        String requestDTO = om.writeValueAsString(postDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna")
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestDTO))
                        .andExpect(status().isBadRequest())
                        .andReturn();

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("productId", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 문의 작성. 상품 아이디가 Blank인 경우")
    void postProductQnAValidationProductIdIsBlank() throws Exception {
        ProductQnAPostDTO postDTO = new ProductQnAPostDTO("", "test insert Product QnA content");
        String requestDTO = om.writeValueAsString(postDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("productId", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 문의 작성. 문의 내용이 null인 경우")
    void postProductQnAValidationContentIsNull() throws Exception {
        ProductQnAPostDTO postDTO = new ProductQnAPostDTO(product.getId(), null);
        String requestDTO = om.writeValueAsString(postDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("content", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 문의 작성. 문의 내용이 Blank인 경우")
    void postProductQnAValidationContentIsBlank() throws Exception {
        ProductQnAPostDTO postDTO = new ProductQnAPostDTO(product.getId(), "");
        String requestDTO = om.writeValueAsString(postDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(2, response.errors().size());

        List<String> contentValidationConstraintList = List.of("NotBlank", "Size");

        response.errors().forEach(v -> {
            assertEquals("content", v.field());
            assertTrue(contentValidationConstraintList.contains(v.constraint()));
        });
    }

    @Test
    @DisplayName(value = "상품 문의 작성. 문의 내용이 한 글자인 경우")
    void postProductQnAValidationContentLength1() throws Exception {
        ProductQnAPostDTO postDTO = new ProductQnAPostDTO(product.getId(), "a");
        String requestDTO = om.writeValueAsString(postDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("content", responseObj.field());
        assertEquals("Size", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 문의 작성. 모든 파라미터가 null인 경우")
    void postProductQnAValidationAllParameterIsNull() throws Exception {
        ProductQnAPostDTO postDTO = new ProductQnAPostDTO(null, null);
        String requestDTO = om.writeValueAsString(postDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(2, response.errors().size());

        Map<String, String> validationMap = new HashMap<>();
        validationMap.put("productId", "NotBlank");
        validationMap.put("content", "NotBlank");

        response.errors().forEach(v -> {
            String constraint = validationMap.getOrDefault(v.field(), null);

            assertNotNull(constraint);
            assertEquals(constraint, v.constraint());
        });
    }

    @Test
    @DisplayName(value = "관심 상품 등록")
    void likeProduct() throws Exception {
        productLikeRepository.deleteAll();
        Map<String, String> params = new HashMap<>();
        params.put("productId", product.getId());
        String requestMap = om.writeValueAsString(params);

        mockMvc.perform(post(URL_PREFIX + "like")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestMap))
                .andExpect(status().isNoContent())
                .andReturn();

        List<ProductLike> likeList = productLikeRepository.findAll();
        assertNotNull(likeList);
        assertFalse(likeList.isEmpty());
        assertEquals(1, likeList.size());

        ProductLike likeData = likeList.get(0);
        assertEquals(product.getId(), likeData.getProduct().getId());
        assertEquals(member.getUserId(), likeData.getMember().getUserId());
    }

    @Test
    @DisplayName(value = "관심 상품 등록. map 데이터가 잘못 전달된 경우")
    void likeProductWrongRequestMap() throws Exception {
        productLikeRepository.deleteAll();
        Map<String, String> params = new HashMap<>();
        params.put("product", product.getId());
        String requestMap = om.writeValueAsString(params);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "like")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestMap))
                .andExpect(status().is(400))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "관심 상품 등록. map 데이터 중 productId가 전달된 경우")
    void likeProductWrongProductId() throws Exception {
        productLikeRepository.deleteAll();
        Map<String, String> params = new HashMap<>();
        params.put("productId", "noneProductId");
        String requestMap = om.writeValueAsString(params);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "like")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestMap))
                .andExpect(status().is(400))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "관심상품 해제")
    void deLikeProduct() throws Exception{
        mockMvc.perform(delete(URL_PREFIX + "like/" + product.getId())
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isNoContent())
                .andReturn();
        List<ProductLike> checkDataList = productLikeRepository.findByMember_UserId(member.getUserId());

        assertNotNull(checkDataList);
        assertFalse(checkDataList.isEmpty());

        boolean flag = true;
        for(ProductLike likeData : checkDataList) {
            if(likeData.getProduct().getId().equals(product.getId())) {
                flag = false;
                break;
            }
        }

        assertTrue(flag);
    }

    @Test
    @DisplayName(value = "관심상품 해제. 잘못된 상품 아이디를 전달하는 경우")
    void deLikeProductWrongProductId() throws Exception{
        MvcResult result = mockMvc.perform(delete(URL_PREFIX + "like/noneProductId")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().is(400))
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );
        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }
}
