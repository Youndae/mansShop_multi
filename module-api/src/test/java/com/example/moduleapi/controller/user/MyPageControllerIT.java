package com.example.moduleapi.controller.user;


import com.example.moduleapi.ModuleApiApplication;
import com.example.moduleapi.config.exception.ExceptionEntity;
import com.example.moduleapi.config.exception.ValidationError;
import com.example.moduleapi.config.exception.ValidationExceptionEntity;
import com.example.moduleapi.fixture.TokenFixture;
import com.example.moduleapi.model.response.PagingResponseDTO;
import com.example.moduleapi.model.response.ResponseIdDTO;
import com.example.modulecommon.customException.CustomNotFoundException;
import com.example.modulecommon.customException.InvalidJoinPolicyException;
import com.example.modulecommon.fixture.*;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.dto.page.MyPagePageDTO;
import com.example.modulecommon.model.dto.qna.in.QnAReplyInsertDTO;
import com.example.modulecommon.model.dto.qna.in.QnAReplyPatchDTO;
import com.example.modulecommon.model.dto.qna.out.QnADetailReplyDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.model.enumuration.MailSuffix;
import com.example.modulecommon.model.enumuration.OrderStatus;
import com.example.modulecommon.utils.TestPaginationUtils;
import com.example.moduleconfig.properties.CookieProperties;
import com.example.moduleconfig.properties.TokenProperties;
import com.example.modulemypage.model.dto.memberQnA.in.MemberQnAInsertDTO;
import com.example.modulemypage.model.dto.memberQnA.in.MemberQnAModifyDTO;
import com.example.modulemypage.model.dto.memberQnA.out.MemberQnADetailResponseDTO;
import com.example.modulemypage.model.dto.memberQnA.out.MemberQnAModifyDataDTO;
import com.example.modulemypage.model.dto.memberQnA.out.QnAClassificationDTO;
import com.example.modulemypage.repository.MemberQnAReplyRepository;
import com.example.modulemypage.repository.MemberQnARepository;
import com.example.modulemypage.repository.QnAClassificationRepository;
import com.example.moduleorder.model.dto.out.OrderListDTO;
import com.example.moduleorder.model.dto.page.OrderPageDTO;
import com.example.moduleorder.repository.ProductOrderDetailRepository;
import com.example.moduleorder.repository.ProductOrderRepository;
import com.example.moduleproduct.model.dto.page.LikePageDTO;
import com.example.moduleproduct.model.dto.productLike.out.ProductLikeDTO;
import com.example.moduleproduct.model.dto.productQnA.out.ProductQnADetailResponseDTO;
import com.example.moduleproduct.model.dto.productQnA.out.ProductQnAListDTO;
import com.example.moduleproduct.model.dto.productReview.in.MyPagePatchReviewDTO;
import com.example.moduleproduct.model.dto.productReview.in.MyPagePostReviewDTO;
import com.example.moduleproduct.model.dto.productReview.out.MyPagePatchReviewDataDTO;
import com.example.moduleproduct.model.dto.productReview.out.MyPageReviewDTO;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productLike.ProductLikeRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import com.example.moduleproduct.repository.productQnA.ProductQnARepository;
import com.example.moduleproduct.repository.productQnAReply.ProductQnAReplyRepository;
import com.example.moduleproduct.repository.productReview.ProductReviewRepository;
import com.example.moduleproduct.repository.productReviewReply.ProductReviewReplyRepository;
import com.example.moduleuser.model.dto.member.in.MyPageInfoPatchDTO;
import com.example.moduleuser.model.dto.member.out.MyPageInfoDTO;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest(classes = ModuleApiApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class MyPageControllerIT {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private EntityManager em;

    @Autowired
    private TokenFixture tokenFixture;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

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
    private ProductLikeRepository productLikeRepository;

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Autowired
    private ProductOrderDetailRepository productOrderDetailRepository;

    @Autowired
    private ProductReviewRepository productReviewRepository;

    @Autowired
    private ProductReviewReplyRepository productReviewReplyRepository;

    @Autowired
    private ProductQnARepository productQnARepository;

    @Autowired
    private ProductQnAReplyRepository productQnAReplyRepository;

    @Autowired
    private QnAClassificationRepository qnAClassificationRepository;

    @Autowired
    private MemberQnARepository memberQnARepository;

    @Autowired
    private MemberQnAReplyRepository memberQnAReplyRepository;

    @Autowired
    private TokenProperties tokenProperties;

    @Autowired
    private CookieProperties cookieProperties;

    private Member member;

    private Member admin;

    private Member noneMember;

    private Map<String, String> tokenMap;

    private String accessTokenValue;

    private String refreshTokenValue;

    private String inoValue;

    private List<ProductOption> productOptionList;

    private List<ProductLike> productLikeList;

    private List<ProductOrder> productOrderList;

    private List<ProductReviewReply> reviewReplyList;

    private List<ProductReview> newReviewList;

    private List<ProductReview> allReviewList;

    private ProductReview noneMemberReview;

    private List<ProductQnAReply> productQnAReplyList;

    private List<ProductQnA> newProductQnAList;

    private List<ProductQnA> allProductQnAList;

    private ProductQnA noneMemberProductQnA;

    private List<QnAClassification> qnAClassificationList;

    private List<MemberQnAReply> memberQnAReplyList;

    private List<MemberQnA> newMemberQnAList;

    private List<MemberQnA> allMemberQnAList;

    private MemberQnA noneMemberQnA;

    private static final String URL_PREFIX = "/api/my-page/";

    @BeforeEach
    void init() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();

        MemberAndAuthFixtureDTO memberAndAuthFixtureDTO = MemberAndAuthFixture.createDefaultMember(2);
        MemberAndAuthFixtureDTO adminFixture = MemberAndAuthFixture.createAdmin();
        List<Member> saveMemberList = new ArrayList<>(memberAndAuthFixtureDTO.memberList());
        saveMemberList.addAll(adminFixture.memberList());
        List<Auth> saveAuthList = new ArrayList<>(memberAndAuthFixtureDTO.authList());
        saveAuthList.addAll(adminFixture.authList());
        memberRepository.saveAll(saveMemberList);
        authRepository.saveAll(saveAuthList);
        member = memberAndAuthFixtureDTO.memberList().get(0);
        noneMember = memberAndAuthFixtureDTO.memberList().get(1);
        admin = adminFixture.memberList().get(0);

        tokenMap = tokenFixture.createAndSaveAllToken(member);
        accessTokenValue = tokenMap.get(tokenProperties.getAccess().getHeader());
        refreshTokenValue = tokenMap.get(tokenProperties.getRefresh().getHeader());
        inoValue = tokenMap.get(cookieProperties.getIno().getHeader());

        List<Classification> classificationList = ClassificationFixture.createClassifications();
        classificationRepository.saveAll(classificationList);

        List<Product> productList = ProductFixture.createProductFixtureList(30, classificationList.get(0));
        productOptionList = productList.stream().flatMap(v -> v.getProductOptions().stream()).toList();
        productRepository.saveAll(productList);
        productOptionRepository.saveAll(productOptionList);

        productLikeList = ProductLikeFixture.createDefaultProductLike(List.of(member), productList);
        productLikeRepository.saveAll(productLikeList);

        productOrderList = ProductOrderFixture.createDefaultProductOrder(List.of(member), productOptionList);
        productOrderRepository.saveAll(productOrderList);

        List<ProductReview> completeAnswerReviewList = ProductReviewFixture.createReviewWithCompletedAnswer(List.of(member), productOptionList);
        noneMemberReview = ProductReviewFixture.createDefaultReview(List.of(noneMember), List.of(productOptionList.get(0))).get(0);
        reviewReplyList = ProductReviewFixture.createDefaultReviewReply(completeAnswerReviewList, admin);
        newReviewList = ProductReviewFixture.createDefaultReview(List.of(member), productOptionList);
        allReviewList = new ArrayList<>(completeAnswerReviewList);
        allReviewList.addAll(newReviewList);
        productReviewRepository.save(noneMemberReview);
        productReviewRepository.saveAll(allReviewList);
        productReviewReplyRepository.saveAll(reviewReplyList);


        List<ProductQnA> completeAnswerProductQnAList = ProductQnAFixture.createProductQnACompletedAnswer(List.of(member), productList);
        noneMemberProductQnA = ProductQnAFixture.createDefaultProductQnA(List.of(noneMember), List.of(productList.get(0))).get(0);
        productQnAReplyList = ProductQnAFixture.createDefaultProductQnaReply(admin, completeAnswerProductQnAList);
        newProductQnAList = ProductQnAFixture.createDefaultProductQnA(List.of(member), productList);
        allProductQnAList = new ArrayList<>(completeAnswerProductQnAList);
        allProductQnAList.addAll(newProductQnAList);
        productQnARepository.save(noneMemberProductQnA);
        productQnARepository.saveAll(allProductQnAList);
        productQnAReplyRepository.saveAll(productQnAReplyList);

        qnAClassificationList = QnAClassificationFixture.createQnAClassificationList();
        qnAClassificationRepository.saveAll(qnAClassificationList);

        List<MemberQnA> completeAnswerMemberQnAList = MemberQnAFixture.createMemberQnACompletedAnswer(qnAClassificationList, List.of(member));
        noneMemberQnA = MemberQnAFixture.createDefaultMemberQnA(qnAClassificationList, List.of(noneMember)).get(0);
        memberQnAReplyList = MemberQnAFixture.createMemberQnAReply(completeAnswerMemberQnAList, admin);
        newMemberQnAList = MemberQnAFixture.createDefaultMemberQnA(qnAClassificationList, List.of(member));
        allMemberQnAList = new ArrayList<>(completeAnswerMemberQnAList);
        allMemberQnAList.addAll(newMemberQnAList);
        memberQnARepository.save(noneMemberQnA);
        memberQnARepository.saveAll(allMemberQnAList);
        memberQnAReplyRepository.saveAll(memberQnAReplyList);

        em.flush();
        em.clear();
    }

    @AfterEach
    void cleanUp() {
        String accessKey = tokenMap.get("accessKey");
        String refreshKey = tokenMap.get("refreshKey");

        redisTemplate.delete(accessKey);
        redisTemplate.delete(refreshKey);
    }

    private long getWrongProductQnAId() {
        return allProductQnAList.stream()
                .mapToLong(ProductQnA::getId)
                .max()
                .getAsLong() + 1;
    }

    private long getWrongQnAClassificationId() {
        return qnAClassificationList.stream()
                .mapToLong(QnAClassification::getId)
                .max()
                .getAsLong() + 1;
    }

    private long getWrongMemberQnAId() {
        return allMemberQnAList.stream()
                .mapToLong(MemberQnA::getId)
                .max()
                .getAsLong() + 1;
    }

    private long getWrongMemberQnAReplyId() {
        return memberQnAReplyList.stream()
                .mapToLong(MemberQnAReply::getId)
                .max()
                .getAsLong() + 1;
    }

    private long getWrongReviewId() {
        return allReviewList.stream()
                .mapToLong(ProductReview::getId)
                .max()
                .getAsLong() + 1;
    }

    private long getWrongProductOptionId() {
        return productOptionList.stream()
                .mapToLong(ProductOption::getId)
                .max()
                .getAsLong() + 1;
    }

    private long getWrongOrderDetailId() {
        return productOrderList.stream()
                .flatMap(v -> v.getProductOrderDetailList().stream())
                .mapToLong(ProductOrderDetail::getId)
                .max()
                .getAsLong() + 1;
    }

    @Test
    @DisplayName(value = "회원 주문 내역 조회")
    void getOrderList() throws Exception {
        OrderPageDTO pageDTO = new OrderPageDTO(1, "3");
        int totalPages = TestPaginationUtils.getTotalPages(productOrderList.size(), pageDTO.amount());
        int contentSize = Math.min(productOrderList.size(), pageDTO.amount());
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "order/3")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<OrderListDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(contentSize, response.content().size());
        assertEquals(totalPages, response.totalPages());
    }

    @Test
    @DisplayName(value = "회원 주문 내역 조회. 데이터가 없는 경우")
    void getOrderListEmpty() throws Exception {
        productOrderRepository.deleteAll();
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "order/3")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<OrderListDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertTrue(response.empty());
        assertEquals(0, response.totalPages());
    }

    @Test
    @DisplayName(value = "회원 주문 내역 조회. term 값이 잘못된 경우")
    void getOrderListValidationTermIsWrong() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "order/8")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원 주문 내역 조회. 페이지 값이 1보다 작은 경우")
    void getOrderListValidationPageIsZero() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "order/3")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("page", "0"))
                .andExpect(status().isBadRequest())
                .andReturn();

        // @Min에 의한 HandlerMethodValidationException을 검증하기 위함
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
    @DisplayName(value = "회원의 관심 상품 목록 조회")
    void getLikeProductList() throws Exception {
        LikePageDTO pageDTO = new LikePageDTO(1);
        int totalPages = TestPaginationUtils.getTotalPages(productLikeList.size(), pageDTO.amount());
        int contentSize = Math.min(productLikeList.size(), pageDTO.amount());
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "like")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<ProductLikeDTO> response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(contentSize, response.content().size());
        assertEquals(totalPages, response.totalPages());
    }

    @Test
    @DisplayName(value = "회원의 관심 상품 목록 조회. 데이터가 없는 경우")
    void getLikeProductListEmpty() throws Exception {
        productLikeRepository.deleteAll();
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "like")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<ProductLikeDTO> response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertTrue(response.empty());
        assertEquals(0, response.totalPages());
    }

    @Test
    @DisplayName(value = "회원의 관심 상품 목록 조회. 페이지값이 1보다 작은 경우")
    void getLikeProductListValidationPageIsZero() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "like")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("page", "0"))
                .andExpect(status().isBadRequest())
                .andReturn();
        // @Min에 의한 HandlerMethodValidationException 검증을 위함
        Exception ex = result.getResolvedException();
        assertInstanceOf(HandlerMethodValidationException.class, ex);

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
        assertNull(response.errors());
    }

    @Test
    @DisplayName(value = "회원의 상품 문의 목록 조회")
    void getProductQnAList() throws Exception {
        MyPagePageDTO pageDTO = new MyPagePageDTO(1);
        int totalPages = TestPaginationUtils.getTotalPages(allProductQnAList.size(), pageDTO.amount());
        int contentSize = Math.min(allProductQnAList.size(), pageDTO.amount());
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<ProductQnAListDTO> response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(contentSize, response.content().size());
        assertEquals(totalPages, response.totalPages());
    }

    @Test
    @DisplayName(value = "회원의 상품 문의 목록 조회. 데이터가 없는 경우")
    void getProductQnAListEmpty() throws Exception {
        productQnARepository.deleteAll();
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<ProductQnAListDTO> response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertTrue(response.empty());
        assertEquals(0, response.totalPages());
    }

    @Test
    @DisplayName(value = "회원의 상품 문의 목록 조회. 페이지값이 1보다 작은 경우")
    void getProductQnAListValidationPageIsZero() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("page", "0"))
                .andExpect(status().isBadRequest())
                .andReturn();

        // @Min에 의한 HandlerMethodValidationException 검증을 위함
        Exception ex = result.getResolvedException();
        assertInstanceOf(HandlerMethodValidationException.class, ex);

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
        assertNull(response.errors());
    }

    @Test
    @DisplayName(value = "회원의 미답변 상품 문의 상세 조회")
    void getProductQnADetail() throws Exception {
        ProductQnA fixture = newProductQnAList.get(0);

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product/detail/" + fixture.getId())
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ProductQnADetailResponseDTO response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(fixture.getId(), response.productQnAId());
        assertEquals(fixture.getProduct().getProductName(), response.productName());
        assertEquals(fixture.getMember().getNickname(), response.writer());
        assertEquals(fixture.getQnaContent(), response.qnaContent());
        assertFalse(response.productQnAStat());
        assertTrue(response.replyList().isEmpty());
    }

    @Test
    @DisplayName(value = "회원의 답변 완료 상품 문의 상세 조회")
    void getCompleteProductQnADetail() throws Exception {
        ProductQnAReply replyFixture = productQnAReplyList.get(0);
        ProductQnA fixture = replyFixture.getProductQnA();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product/detail/" + fixture.getId())
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ProductQnADetailResponseDTO response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(fixture.getId(), response.productQnAId());
        assertEquals(fixture.getProduct().getProductName(), response.productName());
        assertEquals(fixture.getMember().getNickname(), response.writer());
        assertEquals(fixture.getQnaContent(), response.qnaContent());
        assertTrue(response.productQnAStat());
        assertFalse(response.replyList().isEmpty());

        QnADetailReplyDTO responseReply = response.replyList().get(0);

        assertEquals(replyFixture.getId(), responseReply.replyId());
        assertEquals(replyFixture.getMember().getNickname(), responseReply.writer());
        assertEquals(replyFixture.getReplyContent(), responseReply.replyContent());
    }

    @Test
    @DisplayName(value = "회원의 상품 문의 상세 조회. 작성자가 일치하지 않는 경우")
    void getProductQnADetailWriterNotEquals() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product/detail/" + noneMemberProductQnA.getId())
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isForbidden())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.FORBIDDEN.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원의 상품 문의 상세 조회. 데이터가 없는 경우")
    void getProductQnADetailNotFound() throws Exception {
        long wrongProductQnAId = getWrongProductQnAId();
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product/detail/" + wrongProductQnAId)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // 잘못된 아이디로 인해 조회결과가 null인 경우 CustomNotFoundException
        Exception ex = result.getResolvedException();
        assertInstanceOf(CustomNotFoundException.class, ex);

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원의 상품 문의 상세 조회. 문의 아이디가 1보다 작은 경우")
    void getProductQnADetailValidationQnAIdIsZero() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product/detail/0")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // @Min에 의한 HandlerMethodValidationException 검증을 위함
        Exception ex = result.getResolvedException();
        assertInstanceOf(HandlerMethodValidationException.class, ex);

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
        assertNull(response.errors());
    }

    @Test
    @DisplayName(value = "회원의 상품 문의 삭제")
    void deleteProductQnA() throws Exception {
        Long fixtureId = allProductQnAList.get(0).getId();

        mockMvc.perform(delete(URL_PREFIX + "qna/product/" + fixtureId)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isNoContent())
                .andReturn();

        ProductQnA deleteData = productQnARepository.findById(fixtureId).orElse(null);
        assertNull(deleteData);
    }

    @Test
    @DisplayName(value = "회원의 상품 문의 삭제. 작성자가 일치하지 않는 경우")
    void deleteProductQnAWriterNotEquals() throws Exception {
        Long fixtureId = noneMemberProductQnA.getId();
        MvcResult result = mockMvc.perform(delete(URL_PREFIX + "qna/product/" + fixtureId)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isForbidden())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.FORBIDDEN.getMessage(), response.errorMessage());

        ProductQnA deleteData = productQnARepository.findById(fixtureId).orElse(null);
        assertNotNull(deleteData);
    }

    @Test
    @DisplayName(value = "회원의 상품 문의 삭제. 데이터가 없는 경우")
    void deleteProductQnANotFound() throws Exception {
        long wrongProductQnAId = getWrongProductQnAId();
        MvcResult result = mockMvc.perform(delete(URL_PREFIX + "qna/product/" + wrongProductQnAId)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Repository를 통한 조회 과정에서 findById 사용으로 인해 orElse IllegalArgumentException 발생
        Exception ex = result.getResolvedException();
        assertInstanceOf(IllegalArgumentException.class, ex);

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원의 상품 문의 삭제. 문의 아이디 값이 1보다 작은 경우")
    void deleteProductQnAValidationQnAIdIsZero() throws Exception {
        MvcResult result = mockMvc.perform(delete(URL_PREFIX + "qna/product/0")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // @Min에 의한 HandlerMethodValidationException 검증을 위함
        Exception ex = result.getResolvedException();
        assertInstanceOf(HandlerMethodValidationException.class, ex);

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
        assertNull(response.errors());
    }

    @Test
    @DisplayName(value = "회원의 문의 목록 조회")
    void getMemberQnA() throws Exception {
        MyPagePageDTO pageDTO = new MyPagePageDTO(1);
        int totalPages = TestPaginationUtils.getTotalPages(allMemberQnAList.size(), pageDTO.amount());
        int contentSize = Math.min(allMemberQnAList.size(), pageDTO.amount());
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<ProductQnAListDTO> response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(contentSize, response.content().size());
        assertEquals(totalPages, response.totalPages());
    }

    @Test
    @DisplayName(value = "회원의 문의 목록 조회. 데이터가 없는 경우")
    void getMemberQnAEmpty() throws Exception {
        memberQnARepository.deleteAll();
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<ProductQnAListDTO> response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertTrue(response.empty());
        assertEquals(0, response.totalPages());
    }

    @Test
    @DisplayName(value = "회원의 문의 목록 조회. 페이지값이 1보다 작은 경우")
    void getMemberQnAValidationPageIsZero() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("page", "0"))
                .andExpect(status().isBadRequest())
                .andReturn();
        // @Min에 의한 HandlerMethodValidationException 검증을 위함
        Exception ex = result.getResolvedException();
        assertInstanceOf(HandlerMethodValidationException.class, ex);

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
        assertNull(response.errors());
    }

    @Test
    @DisplayName(value = "회원 문의 작성")
    void postMemberQnA() throws Exception {
        MemberQnAInsertDTO insertDTO = new MemberQnAInsertDTO(
                "test insert title",
                "test insert content",
                qnAClassificationList.get(0).getId()
        );
        String requestDTO = om.writeValueAsString(insertDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna/member")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isCreated())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ResponseIdDTO<Long> response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);

        Long saveId = response.id();

        MemberQnA saveEntity = memberQnARepository.findById(saveId).orElse(null);
        assertNotNull(saveEntity);
        assertEquals(insertDTO.title(), saveEntity.getMemberQnATitle());
        assertEquals(insertDTO.content(), saveEntity.getMemberQnAContent());
        assertEquals(insertDTO.classificationId(), saveEntity.getQnAClassification().getId());
        assertEquals(member.getUserId(), saveEntity.getMember().getUserId());
    }

    @Test
    @DisplayName(value = "회원 문의 작성. 분류 아이디가 잘못된 경우")
    void postMemberQnAWrongQnAClassificationId() throws Exception {
        long wrongQnAClassificationId = getWrongQnAClassificationId();
        MemberQnAInsertDTO insertDTO = new MemberQnAInsertDTO(
                "test insert title",
                "test insert content",
                wrongQnAClassificationId
        );
        String requestDTO = om.writeValueAsString(insertDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna/member")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();

        // repository 호출 시 findById 호출로 인해 orElse IllegalArgumentException 발생
        Exception ex = result.getResolvedException();
        assertInstanceOf(IllegalArgumentException.class, ex);

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원 문의 작성. 제목이 null인 경우")
    void postMemberQnAValidationTitleIsNull() throws Exception {
        MemberQnAInsertDTO insertDTO = new MemberQnAInsertDTO(
                null,
                "test insert content",
                qnAClassificationList.get(0).getId()
        );
        String requestDTO = om.writeValueAsString(insertDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna/member")
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
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("title", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원 문의 작성. 제목이 Blank인 경우")
    void postMemberQnAValidationTitleIsBlank() throws Exception {
        MemberQnAInsertDTO insertDTO = new MemberQnAInsertDTO(
                "",
                "test insert content",
                qnAClassificationList.get(0).getId()
        );
        String requestDTO = om.writeValueAsString(insertDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna/member")
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
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(2, response.errors().size());

        List<String> titleConstraintValidationList = List.of("NotBlank", "Size");

        response.errors().forEach(v -> {
            assertEquals("title", v.field());
            assertTrue(titleConstraintValidationList.contains(v.constraint()));
        });
    }

    @Test
    @DisplayName(value = "회원 문의 작성. 제목이 한글자인 경우")
    void postMemberQnAValidationTitleLength1() throws Exception {
        MemberQnAInsertDTO insertDTO = new MemberQnAInsertDTO(
                "a",
                "test insert content",
                qnAClassificationList.get(0).getId()
        );
        String requestDTO = om.writeValueAsString(insertDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna/member")
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
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("title", responseObj.field());
        assertEquals("Size", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원 문의 작성. 내용이 null인 경우")
    void postMemberQnAValidationContentIsNull() throws Exception {
        MemberQnAInsertDTO insertDTO = new MemberQnAInsertDTO(
                "test insert title",
                null,
                qnAClassificationList.get(0).getId()
        );
        String requestDTO = om.writeValueAsString(insertDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna/member")
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
                new TypeReference<>(){}
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
    @DisplayName(value = "회원 문의 작성. 내용이 Blank인 경우")
    void postMemberQnAValidationContentIsBlank() throws Exception {
        MemberQnAInsertDTO insertDTO = new MemberQnAInsertDTO(
                "test insert title",
                "",
                qnAClassificationList.get(0).getId()
        );
        String requestDTO = om.writeValueAsString(insertDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna/member")
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
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(2, response.errors().size());

        List<String> contentConstraintValidationList = List.of("NotBlank", "Size");

        response.errors().forEach(v -> {
            assertEquals("content", v.field());
            assertTrue(contentConstraintValidationList.contains(v.constraint()));
        });
    }

    @Test
    @DisplayName(value = "회원 문의 작성. 내용이 한글자인 경우")
    void postMemberQnAValidationContentLength1() throws Exception {
        MemberQnAInsertDTO insertDTO = new MemberQnAInsertDTO(
                "test insert title",
                "a",
                qnAClassificationList.get(0).getId()
        );
        String requestDTO = om.writeValueAsString(insertDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna/member")
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
                new TypeReference<>(){}
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
    @DisplayName(value = "회원 문의 작성. 문의 분류 아이디가 1보다 작은 경우")
    void postMemberQnAValidationClassificationIdIsZero() throws Exception {
        MemberQnAInsertDTO insertDTO = new MemberQnAInsertDTO(
                "test insert title",
                "test insert content",
                0L
        );
        String requestDTO = om.writeValueAsString(insertDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna/member")
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
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("classificationId", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원 문의 작성. 제목과 내용이 각각 한글자, 분류 아이디가 0인 경우")
    void postMemberQnAValidationAllParameterIsWrong() throws Exception {
        MemberQnAInsertDTO insertDTO = new MemberQnAInsertDTO(
                "a",
                "a",
                0L
        );
        String requestDTO = om.writeValueAsString(insertDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna/member")
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
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(3, response.errors().size());

        Map<String, String> validationMap = new HashMap<>();
        validationMap.put("title", "Size");
        validationMap.put("content", "Size");
        validationMap.put("classificationId", "Min");

        response.errors().forEach(v -> {
            String constraint = validationMap.getOrDefault(v.field(), null);

            assertNotNull(constraint);
            assertEquals(constraint, v.constraint());
        });
    }

    @Test
    @DisplayName(value = "답변 완료 회원 문의 상세 조회")
    void getMemberQnADetail() throws Exception {
        MemberQnA fixture = allMemberQnAList.stream()
                .filter(MemberQnA::isMemberQnAStat)
                .findFirst()
                .get();
        List<MemberQnAReply> replyFixture = memberQnAReplyList.stream()
                .filter(v ->
                        v.getMemberQnA().getId().equals(fixture.getId())
                )
                .toList();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member/detail/" + fixture.getId())
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        MemberQnADetailResponseDTO response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(fixture.getId(), response.memberQnAId());
        assertEquals(fixture.getQnAClassification().getQnaClassificationName(), response.qnaClassification());
        assertEquals(fixture.getMemberQnATitle(), response.qnaTitle());
        assertEquals(fixture.getMember().getNickname(), response.writer());
        assertEquals(fixture.getMemberQnAContent(), response.qnaContent());
        assertEquals(fixture.isMemberQnAStat(), response.memberQnAStat());
        assertEquals(replyFixture.size(), response.replyList().size());

        for(QnADetailReplyDTO responseReply : response.replyList()) {
            for(MemberQnAReply reply : replyFixture) {
                if(responseReply.replyId() == reply.getId()) {
                    assertEquals(reply.getMember().getNickname(), responseReply.writer());
                    assertEquals(reply.getReplyContent(), responseReply.replyContent());
                }
            }
        }
    }

    @Test
    @DisplayName(value = "미답변 회원 문의 상세 조회")
    void getNewMemberQnADetail() throws Exception {
        MemberQnA fixture = newMemberQnAList.get(0);
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member/detail/" + fixture.getId())
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        MemberQnADetailResponseDTO response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(fixture.getId(), response.memberQnAId());
        assertEquals(fixture.getQnAClassification().getQnaClassificationName(), response.qnaClassification());
        assertEquals(fixture.getMemberQnATitle(), response.qnaTitle());
        assertEquals(fixture.getMember().getNickname(), response.writer());
        assertEquals(fixture.getMemberQnAContent(), response.qnaContent());
        assertFalse(response.memberQnAStat());
        assertTrue(response.replyList().isEmpty());
    }

    @Test
    @DisplayName(value = "회원 문의 상세 조회. 작성자가 일치하지 않는 경우")
    void getMemberQnADetailWriterNotEquals() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member/detail/" + noneMemberQnA.getId())
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isForbidden())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.FORBIDDEN.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원 문의 상세 조회. 데이터가 없는 경우")
    void getMemberQnADetailNotFound() throws Exception {
        long wrongMemberQnAId = getWrongMemberQnAId();
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member/detail/" + wrongMemberQnAId)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // 데이터 조회 결과가 null인 경우 NotFoundException 발생
        Exception ex = result.getResolvedException();
        assertInstanceOf(CustomNotFoundException.class, ex);

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원 문의 상세 조회. 문의 아이디가 1보다 작은 경우")
    void getMemberQnADetailValidationQnAIdIsZero() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member/detail/0")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // @Min에 의한 HandlerMethodValidationException 검증을 위함
        Exception ex = result.getResolvedException();
        assertInstanceOf(HandlerMethodValidationException.class, ex);

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
        assertNull(response.errors());
    }

    @Test
    @DisplayName(value = "회원 문의 답변 작성")
    void postMemberQnAReply() throws Exception {
        MemberQnA fixture = allMemberQnAList.stream()
                .filter(MemberQnA::isMemberQnAStat)
                .findFirst()
                .get();
        int replySize = memberQnAReplyList.stream()
                .filter(v ->
                        v.getMemberQnA().getId().equals(fixture.getId())
                )
                .toList()
                .size();
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(fixture.getId(), "test reply content");
        String requestDTO = om.writeValueAsString(insertDTO);

        mockMvc.perform(post(URL_PREFIX + "qna/member/reply")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isOk())
                .andReturn();

        MemberQnA patchMemberQnA = memberQnARepository.findById(fixture.getId()).orElse(null);
        assertNotNull(patchMemberQnA);
        assertFalse(patchMemberQnA.isMemberQnAStat());

        List<QnADetailReplyDTO> replyList = memberQnAReplyRepository.findAllByQnAId(fixture.getId());
        assertNotNull(replyList);
        assertFalse(replyList.isEmpty());
        assertEquals(replySize + 1, replyList.size());

        QnADetailReplyDTO saveReply = replyList.get(replyList.size() - 1);
        assertEquals(member.getNickname(), saveReply.writer());
        assertEquals(insertDTO.content(), saveReply.replyContent());
    }

    @Test
    @DisplayName(value = "회원 문의 답변 작성. 회원 문의 데이터가 없는 경우")
    void postMemberQnAReplyNotFound() throws Exception {
        long wrongMemberQnAId = getWrongMemberQnAId();
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(wrongMemberQnAId, "test reply content");
        String requestDTO = om.writeValueAsString(insertDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna/member/reply")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();

        // findById를 통한 조회로 orElse IllegalArgumentException 발생
        Exception ex = result.getResolvedException();
        assertInstanceOf(IllegalArgumentException.class, ex);

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원 문의 답변 작성. 작성자가 일치하지 않는 경우")
    void postMemberQnAReplyWriterNotEquals() throws Exception {
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(noneMemberQnA.getId(), "test reply content");
        String requestDTO = om.writeValueAsString(insertDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna/member/reply")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isForbidden())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.FORBIDDEN.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원 문의 답변 작성. 문의 아이디가 1보다 작은 경우")
    void postMemberQnAReplyValidationQnAIdIsZero() throws Exception {
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(0L, "test reply content");
        String requestDTO = om.writeValueAsString(insertDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna/member/reply")
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

        assertEquals("qnaId", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원 문의 답변 작성. 문의 내용이 null인 경우")
    void postMemberQnAReplyValidationContentIsNull() throws Exception {
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(1L, null);
        String requestDTO = om.writeValueAsString(insertDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna/member/reply")
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
    @DisplayName(value = "회원 문의 답변 작성. 문의 내용이 Blank인 경우")
    void postMemberQnAReplyValidationContentIsBlank() throws Exception {
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(1L, "");
        String requestDTO = om.writeValueAsString(insertDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna/member/reply")
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
    @DisplayName(value = "회원 문의 답변 작성. 문의 아이디가 0, 내용이 null인 경우")
    void postMemberQnAReplyValidationQnAIdIsZeroAndContentIsNull() throws Exception {
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(0L, null);
        String requestDTO = om.writeValueAsString(insertDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna/member/reply")
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
        validationMap.put("qnaId", "Min");
        validationMap.put("content", "NotBlank");

        response.errors().forEach(v -> {
            String constraint = validationMap.getOrDefault(v.field(), null);

            assertNotNull(constraint);
            assertEquals(constraint, v.constraint());
        });
    }

    @Test
    @DisplayName(value = "회원 문의 답변 수정")
    void patchMemberQnAReply() throws Exception {
        MemberQnAReply fixture = memberQnAReplyList.stream()
                .filter(v ->
                        v.getMember().getUserId().equals(member.getUserId())
                )
                .findFirst()
                .get();
        QnAReplyPatchDTO replyDTO = new QnAReplyPatchDTO(fixture.getId(), "modify memberQnA Reply content");
        String requestDTO = om.writeValueAsString(replyDTO);

        mockMvc.perform(patch(URL_PREFIX + "qna/member/reply")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isNoContent())
                .andReturn();

        MemberQnAReply patchData = memberQnAReplyRepository.findById(fixture.getId()).orElse(null);
        assertNotNull(patchData);
        assertEquals(replyDTO.content(), patchData.getReplyContent());
    }

    @Test
    @DisplayName(value = "회원 문의 답변 수정. 답변 데이터가 없는 경우")
    void patchMemberQnAReplyEmpty() throws Exception {
        long wrongMemberQnAReplyId = getWrongMemberQnAReplyId();
        QnAReplyPatchDTO replyDTO = new QnAReplyPatchDTO(wrongMemberQnAReplyId, "modify memberQnA Reply content");
        String requestDTO = om.writeValueAsString(replyDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/member/reply")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();

        // findById를 통한 조회로 orElse IllegalArgumentException 발생
        Exception ex = result.getResolvedException();
        assertInstanceOf(IllegalArgumentException.class, ex);

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원 문의 답변 수정. 작성자가 일치하지 않는 경우")
    void patchMemberQnAReplyWriterNotEquals() throws Exception {
        MemberQnAReply fixture = memberQnAReplyList.stream()
                .filter(v ->
                        v.getMember().getUserId().equals(admin.getUserId())
                )
                .findFirst()
                .get();
        QnAReplyPatchDTO replyDTO = new QnAReplyPatchDTO(fixture.getId(), "modify memberQnA Reply content");
        String requestDTO = om.writeValueAsString(replyDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/member/reply")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isForbidden())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.FORBIDDEN.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원 문의 답변 수정. 답변 아이디가 1보다 작은 경우")
    void patchMemberQnAReplyValidationReplyIdIsZero() throws Exception {
        QnAReplyPatchDTO replyDTO = new QnAReplyPatchDTO(0L, "modify memberQnA Reply content");
        String requestDTO = om.writeValueAsString(replyDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/member/reply")
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
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("replyId", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원 문의 답변 수정. 답변 내용이 null인 경우")
    void patchMemberQnAReplyValidationContentIsNull() throws Exception {
        QnAReplyPatchDTO replyDTO = new QnAReplyPatchDTO(1L, null);
        String requestDTO = om.writeValueAsString(replyDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/member/reply")
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
                new TypeReference<>(){}
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
    @DisplayName(value = "회원 문의 답변 수정. 답변 내용이 Blank인 경우")
    void patchMemberQnAReplyValidationContentIsBlank() throws Exception {
        QnAReplyPatchDTO replyDTO = new QnAReplyPatchDTO(1L, "");
        String requestDTO = om.writeValueAsString(replyDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/member/reply")
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
                new TypeReference<>(){}
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
    @DisplayName(value = "회원 문의 답변 수정. 답변 아이디가 0, 내용이 null인 경우")
    void patchMemberQnAReplyValidationReplyIdIsZeroAndContentIsNull() throws Exception {
        QnAReplyPatchDTO replyDTO = new QnAReplyPatchDTO(0L, null);
        String requestDTO = om.writeValueAsString(replyDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/member/reply")
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
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(2, response.errors().size());

        Map<String, String> validationMap = new HashMap<>();
        validationMap.put("replyId", "Min");
        validationMap.put("content", "NotBlank");

        response.errors().forEach(v -> {
            String constraint = validationMap.getOrDefault(v.field(), null);

            assertNotNull(constraint);
            assertEquals(constraint, v.constraint());
        });
    }

    @Test
    @DisplayName(value = "회원 문의 수정을 위한 데이터 조회")
    void getModifyData() throws Exception {
        MemberQnA fixture = allMemberQnAList.get(0);

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member/modify/" + fixture.getId())
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        MemberQnAModifyDataDTO response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(fixture.getId(), response.qnaId());
        assertEquals(fixture.getMemberQnATitle(), response.qnaTitle());
        assertEquals(fixture.getMemberQnAContent(), response.qnaContent());
        assertEquals(fixture.getQnAClassification().getId(), response.qnaClassificationId());
        assertEquals(qnAClassificationList.size(), response.classificationList().size());
    }

    @Test
    @DisplayName(value = "회원 문의 수정을 위한 데이터 조회. 데이터가 없는 경우")
    void getModifyDataEmpty() throws Exception {
        long wrongMemberQnAId = getWrongMemberQnAId();
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member/modify/" + wrongMemberQnAId)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // 잘못된 아이디 기반 요청으로 IllegalArgumentException이 발생
        Exception ex = result.getResolvedException();
        assertInstanceOf(IllegalArgumentException.class, ex);

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원 문의 수정을 위한 데이터 조회. 작성자가 일치하지 않는 경우")
    void getModifyDataWriterNotEquals() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member/modify/" + noneMemberQnA.getId())
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원 문의 수정을 위한 데이터 조회. 문의 아이디가 1보다 작은 경우")
    void getModifyDataValidationQnAIdIsZero() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member/modify/0")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // @Min에 의한 HandlerMethodValidationException 검증을 위함
        Exception ex = result.getResolvedException();
        assertInstanceOf(HandlerMethodValidationException.class, ex);

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
        assertNull(response.errors());
    }

    @Test
    @DisplayName(value = "회원 문의 수정")
    void patchMemberQnA() throws Exception {
        MemberQnA fixture = allMemberQnAList.stream()
                .filter(v ->
                        v.getMember().getUserId().equals(member.getUserId())
                )
                .findFirst()
                .get();
        MemberQnAModifyDTO modifyDTO = new MemberQnAModifyDTO(
                fixture.getId(),
                "test modify title",
                "test modify content",
                fixture.getQnAClassification().getId()
        );
        String requestDTO = om.writeValueAsString(modifyDTO);

        mockMvc.perform(patch(URL_PREFIX + "qna/member")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isNoContent())
                .andReturn();

        MemberQnA patchData = memberQnARepository.findById(fixture.getId()).orElse(null);
        assertNotNull(patchData);
        assertEquals(modifyDTO.title(), patchData.getMemberQnATitle());
        assertEquals(modifyDTO.content(), patchData.getMemberQnAContent());
        assertEquals(modifyDTO.classificationId(), patchData.getQnAClassification().getId());
    }

    @Test
    @DisplayName(value = "회원 문의 수정. 데이터가 없는 경우")
    void patchMemberQnANotFound() throws Exception {
        long wrongMemberQnAId = getWrongMemberQnAId();
        MemberQnAModifyDTO modifyDTO = new MemberQnAModifyDTO(
                wrongMemberQnAId,
                "test modify title",
                "test modify content",
                qnAClassificationList.get(0).getId()
        );
        String requestDTO = om.writeValueAsString(modifyDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/member")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();

        // findById를 통한 조회로 orElse IllegalArgumentException 발생
        Exception ex = result.getResolvedException();
        assertInstanceOf(IllegalArgumentException.class, ex);

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원 문의 수정. 작성자가 일치하지 않는 경우")
    void patchMemberQnAWriterNotEquals() throws Exception {
        MemberQnAModifyDTO modifyDTO = new MemberQnAModifyDTO(
                noneMemberQnA.getId(),
                "test modify title",
                "test modify content",
                noneMemberQnA.getQnAClassification().getId()
        );
        String requestDTO = om.writeValueAsString(modifyDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/member")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isForbidden())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.FORBIDDEN.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원 문의 수정. 문의 아이디가 1보다 작은 경우")
    void patchMemberQnAValidationQnAIdIsZero() throws Exception {
        MemberQnAModifyDTO modifyDTO = new MemberQnAModifyDTO(
                0L,
                "test modify title",
                "test modify content",
                1L
        );
        String requestDTO = om.writeValueAsString(modifyDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/member")
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

        assertEquals("qnaId", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원 문의 수정. 문의 제목이 null인 경우")
    void patchMemberQnAValidationTitleIsNull() throws Exception {
        MemberQnAModifyDTO modifyDTO = new MemberQnAModifyDTO(
                1L,
                null,
                "test modify content",
                1L
        );
        String requestDTO = om.writeValueAsString(modifyDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/member")
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

        assertEquals("title", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원 문의 수정. 문의 제목이 Blank인 경우")
    void patchMemberQnAValidationTitleIsBlank() throws Exception {
        MemberQnAModifyDTO modifyDTO = new MemberQnAModifyDTO(
                1L,
                "",
                "test modify content",
                1L
        );
        String requestDTO = om.writeValueAsString(modifyDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/member")
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

        List<String> titleConstraintValidationList = List.of("NotBlank", "Size");

        response.errors().forEach(v -> {
            assertEquals("title", v.field());
            assertTrue(titleConstraintValidationList.contains(v.constraint()));
        });
    }

    @Test
    @DisplayName(value = "회원 문의 수정. 문의 제목이 한글자인 경우")
    void patchMemberQnAValidationTitleLength1() throws Exception {
        MemberQnAModifyDTO modifyDTO = new MemberQnAModifyDTO(
                1L,
                "a",
                "test modify content",
                1L
        );
        String requestDTO = om.writeValueAsString(modifyDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/member")
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

        assertEquals("title", responseObj.field());
        assertEquals("Size", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원 문의 수정. 문의 내용이 null인 경우")
    void patchMemberQnAValidationContentIsNull() throws Exception {
        MemberQnAModifyDTO modifyDTO = new MemberQnAModifyDTO(
                1L,
                "test modify title",
                null,
                1L
        );
        String requestDTO = om.writeValueAsString(modifyDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/member")
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
    @DisplayName(value = "회원 문의 수정. 문의 내용이 Blank인 경우")
    void patchMemberQnAValidationContentIsBlank() throws Exception {
        MemberQnAModifyDTO modifyDTO = new MemberQnAModifyDTO(
                1L,
                "test modify title",
                "",
                1L
        );
        String requestDTO = om.writeValueAsString(modifyDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/member")
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

        List<String> contentConstraintValidationList = List.of("NotBlank", "Size");

        response.errors().forEach(v -> {
            assertEquals("content", v.field());
            assertTrue(contentConstraintValidationList.contains(v.constraint()));
        });
    }

    @Test
    @DisplayName(value = "회원 문의 수정. 문의 내용이 한글자인 경우")
    void patchMemberQnAValidationContentLength1() throws Exception {
        MemberQnAModifyDTO modifyDTO = new MemberQnAModifyDTO(
                1L,
                "test modify title",
                "a",
                1L
        );
        String requestDTO = om.writeValueAsString(modifyDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/member")
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
    @DisplayName(value = "회원 문의 수정. 문의 분류 아이디가 1보다 작은 경우")
    void patchMemberQnAValidationClassificationIdIsZero() throws Exception {
        MemberQnAModifyDTO modifyDTO = new MemberQnAModifyDTO(
                1L,
                "test modify title",
                "test modify content",
                0L
        );
        String requestDTO = om.writeValueAsString(modifyDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/member")
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

        assertEquals("classificationId", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원 문의 수정. 모든 필드값이 유효성 검사를 통과하지 못한 경우")
    void patchMemberQnAValidationAllParameterIsWrong() throws Exception {
        MemberQnAModifyDTO modifyDTO = new MemberQnAModifyDTO(
                0L,
                null,
                null,
                0L
        );
        String requestDTO = om.writeValueAsString(modifyDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/member")
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

        assertEquals(4, response.errors().size());

        Map<String, String> validationMap = new HashMap<>();
        validationMap.put("qnaId", "Min");
        validationMap.put("title", "NotBlank");
        validationMap.put("content", "NotBlank");
        validationMap.put("classificationId", "Min");

        response.errors().forEach(v -> {
            String constraint = validationMap.getOrDefault(v.field(), null);

            assertNotNull(constraint);
            assertEquals(constraint, v.constraint());
        });
    }

    @Test
    @DisplayName(value = "회원 문의 삭제")
    void deleteMemberQnA() throws Exception {
        long deleteId = allMemberQnAList.stream()
                .filter(v ->
                        v.getMember().getUserId().equals(member.getUserId())
                )
                .findFirst()
                .get()
                .getId();
        mockMvc.perform(delete(URL_PREFIX + "qna/member/" + deleteId)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isNoContent())
                .andReturn();

        MemberQnA deleteData = memberQnARepository.findById(deleteId).orElse(null);
        assertNull(deleteData);
    }

    @Test
    @DisplayName(value = "회원 문의 삭제. 데이터가 없는 경우")
    void deleteMemberQnANotFound() throws Exception {
        long wrongMemberQnAId = getWrongMemberQnAId();
        MvcResult result = mockMvc.perform(delete(URL_PREFIX + "qna/member/" + wrongMemberQnAId)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // findById를 통한 조회로 orElse IllegalArgumentException 발생
        Exception ex = result.getResolvedException();
        assertInstanceOf(IllegalArgumentException.class, ex);

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원 문의 삭제. 작성자가 일치하지 않는 경우")
    void deleteMemberQnAWriterNotEquals() throws Exception {
        MvcResult result = mockMvc.perform(delete(URL_PREFIX + "qna/member/" + noneMemberQnA.getId())
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isForbidden())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.FORBIDDEN.getMessage(), response.errorMessage());

        MemberQnA checkData = memberQnARepository.findById(noneMemberQnA.getId()).orElse(null);
        assertNotNull(checkData);
    }

    @Test
    @DisplayName(value = "회원 문의 삭제. 문의 아이디가 1보다 작은 경우")
    void deleteMemberQnAValidationQnAIdIsZero() throws Exception {
        MvcResult result = mockMvc.perform(delete(URL_PREFIX + "qna/member/0")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // @Min에 의한 HandlerMethodValidationException 검증을 위함
        Exception ex = result.getResolvedException();
        assertInstanceOf(HandlerMethodValidationException.class, ex);

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
        assertNull(response.errors());
    }

    @Test
    @DisplayName(value = "회원 문의 분류 목록 조회")
    void getQnAClassification() throws Exception {
        List<QnAClassificationDTO> fixture = qnAClassificationList.stream()
                .map(v ->
                        new QnAClassificationDTO(v.getId(), v.getQnaClassificationName())
                )
                .toList();
        fixture.forEach(v -> System.out.println("fixture : " + v));
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/classification")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        System.out.println("result content : " + content);
        List<QnAClassificationDTO> response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);

        response.forEach(v -> assertTrue(fixture.contains(v)));
    }

    @Test
    @DisplayName(value = "회원 문의 분류 목록 조회. 데이터가 없는 경우")
    void getQnAClassificationEmpty() throws Exception {
        qnAClassificationRepository.deleteAll();
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/classification")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        List<QnAClassificationDTO> response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    @DisplayName(value = "작성한 리뷰 목록 조회")
    void getReview() throws Exception {
        MyPagePageDTO pageDTO = new MyPagePageDTO(1);
        int contentSize = Math.min(allReviewList.size(), pageDTO.amount());
        int totalPages = TestPaginationUtils.getTotalPages(allReviewList.size(), pageDTO.amount());

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "review")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<MyPageReviewDTO> response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(totalPages, response.totalPages());
        assertEquals(contentSize, response.content().size());
    }

    @Test
    @DisplayName(value = "작성한 리뷰 목록 조회. 데이터가 없는 경우")
    void getReviewEmpty() throws Exception {
        productReviewRepository.deleteAll();
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "review")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<MyPageReviewDTO> response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertTrue(response.empty());
        assertEquals(0, response.totalPages());
    }

    @Test
    @DisplayName(value = "작성한 리뷰 목록 조회. 페이지값이 1보다 작은 경우")
    void getReviewValidationPageIsZero() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "review")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("page", "0"))
                .andExpect(status().isBadRequest())
                .andReturn();

        // @Min에 의한 HandlerMethodValidationException 검증을 위함
        Exception ex = result.getResolvedException();
        assertInstanceOf(HandlerMethodValidationException.class, ex);

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
        assertNull(response.errors());
    }

    @Test
    @DisplayName(value = "리뷰 수정을 위한 데이터 조회")
    void getModifyReviewData() throws Exception {
        ProductReview fixture = allReviewList.stream()
                .filter(v ->
                        v.getMember().getUserId().equals(member.getUserId())
                )
                .findFirst()
                .get();
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "review/modify/" + fixture.getId())
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        MyPagePatchReviewDataDTO response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(fixture.getId(), response.reviewId());
        assertEquals(fixture.getReviewContent(), response.content());
        assertEquals(fixture.getProduct().getProductName(), response.productName());
    }

    @Test
    @DisplayName(value = "리뷰 수정을 위한 데이터 조회. 데이터가 없는 경우")
    void getModifyReviewDataNotFound() throws Exception {
        long wrongReviewId = getWrongReviewId();
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "review/modify/" + wrongReviewId)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // findById를 통한 조회로 orElse IllegalArgumentException 발생
        Exception ex = result.getResolvedException();
        assertInstanceOf(IllegalArgumentException.class, ex);

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertNotNull(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "리뷰 수정을 위한 데이터 조회. 작성자가 일치하지 않는 경우")
    void getModifyReviewDataWriterNotEquals() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "review/modify/" + noneMemberReview.getId())
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isForbidden())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertNotNull(ErrorCode.FORBIDDEN.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "리뷰 수정을 위한 데이터 조회. 리뷰 아이디가 1보다 작은 경우")
    void getModifyReviewDataValidationReviewIdIsZero() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "review/modify/0")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // @Min에 의한 HandlerMethodValidationException 검증을 위함
        Exception ex = result.getResolvedException();
        assertInstanceOf(HandlerMethodValidationException.class, ex);

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
        assertNull(response.errors());
    }

    @Test
    @DisplayName(value = "리뷰 작성")
    void postReview() throws Exception {
        productReviewRepository.deleteAll();
        ProductOrder fixture = productOrderList.stream()
                .filter(v ->
                        v.getMember().getUserId().equals(member.getUserId())
                )
                .findFirst()
                .get();
        fixture.setOrderStat(OrderStatus.COMPLETE.getStatusStr());
        productOrderRepository.save(fixture);

        ProductOrderDetail detailFixture = fixture.getProductOrderDetailList().stream()
                .filter(v ->
                        !v.isOrderReviewStatus()
                )
                .findFirst()
                .get();
        MyPagePostReviewDTO reviewDTO = new MyPagePostReviewDTO(
                detailFixture.getProduct().getId(),
                "test insert review content",
                detailFixture.getProductOption().getId(),
                detailFixture.getId()
        );
        String requestDTO = om.writeValueAsString(reviewDTO);

        mockMvc.perform(post(URL_PREFIX + "review")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isNoContent())
                .andReturn();

        List<ProductReview> reviewList = productReviewRepository.findAll();
        assertFalse(reviewList.isEmpty());
        ProductReview saveReview = reviewList.get(0);

        assertEquals(member.getUserId(), saveReview.getMember().getUserId());
        assertEquals(reviewDTO.productId(), saveReview.getProduct().getId());
        assertEquals(reviewDTO.optionId(), saveReview.getProductOption().getId());

        ProductOrderDetail patchData = productOrderDetailRepository.findById(detailFixture.getId()).orElse(null);
        assertNotNull(patchData);
        assertTrue(patchData.isOrderReviewStatus());
    }

    @Test
    @DisplayName(value = "리뷰 작성. 상품 아이디가 잘못 된 경우")
    void postReviewWrongProductId() throws Exception {
        ProductOrder fixture = productOrderList.stream()
                .filter(v ->
                        v.getMember().getUserId().equals(member.getUserId())
                )
                .findFirst()
                .get();

        ProductOrderDetail detailFixture = fixture.getProductOrderDetailList().stream()
                .filter(v ->
                        !v.isOrderReviewStatus()
                )
                .findFirst()
                .get();
        MyPagePostReviewDTO reviewDTO = new MyPagePostReviewDTO(
                "noneProductId",
                "test insert review content",
                detailFixture.getProductOption().getId(),
                detailFixture.getId()
        );
        String requestDTO = om.writeValueAsString(reviewDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "review")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "리뷰 작성. 상품 옵션 아이디가 잘못 된 경우")
    void postReviewWrongProductOptionId() throws Exception {
        long wrongOptionId = getWrongProductOptionId();
        ProductOrder fixture = productOrderList.stream()
                .filter(v ->
                        v.getMember().getUserId().equals(member.getUserId())
                )
                .findFirst()
                .get();

        ProductOrderDetail detailFixture = fixture.getProductOrderDetailList().stream()
                .filter(v ->
                        !v.isOrderReviewStatus()
                )
                .findFirst()
                .get();
        MyPagePostReviewDTO reviewDTO = new MyPagePostReviewDTO(
                detailFixture.getProduct().getId(),
                "test insert review content",
                wrongOptionId,
                detailFixture.getId()
        );
        String requestDTO = om.writeValueAsString(reviewDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "review")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();

        // findById를 통한 조회로 orElse IllegalArgumentException 발생
        Exception ex = result.getResolvedException();
        assertInstanceOf(IllegalArgumentException.class, ex);

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "리뷰 작성. 주문 상세 아이디가 잘못 된 경우")
    void postReviewWrongOrderDetailId() throws Exception {
        long wrongOrderDetailId = getWrongOrderDetailId();
        ProductOrder fixture = productOrderList.stream()
                .filter(v ->
                        v.getMember().getUserId().equals(member.getUserId())
                )
                .findFirst()
                .get();
        fixture.setOrderStat(OrderStatus.COMPLETE.getStatusStr());
        productOrderRepository.save(fixture);

        ProductOrderDetail detailFixture = fixture.getProductOrderDetailList().stream()
                .filter(v ->
                        !v.isOrderReviewStatus()
                )
                .findFirst()
                .get();
        MyPagePostReviewDTO reviewDTO = new MyPagePostReviewDTO(
                detailFixture.getProduct().getId(),
                "test insert review content",
                detailFixture.getProductOption().getId(),
                wrongOrderDetailId
        );
        String requestDTO = om.writeValueAsString(reviewDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "review")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();

        // findById를 통한 조회로 orElse IllegalArgumentException 발생
        Exception ex = result.getResolvedException();
        assertInstanceOf(IllegalArgumentException.class, ex);

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "리뷰 작성. 주문 상태가 배송 완료가 아닌 경우")
    void postReviewOrderStatusIsNotComplete() throws Exception {
        ProductOrder fixture = productOrderList.stream()
                .filter(v ->
                        v.getMember().getUserId().equals(member.getUserId())
                )
                .findFirst()
                .get();
        fixture.setOrderStat(OrderStatus.ORDER.getStatusStr());
        productOrderRepository.save(fixture);

        ProductOrderDetail detailFixture = fixture.getProductOrderDetailList().stream()
                .filter(v ->
                        !v.isOrderReviewStatus()
                )
                .findFirst()
                .get();
        MyPagePostReviewDTO reviewDTO = new MyPagePostReviewDTO(
                detailFixture.getProduct().getId(),
                "test insert review content",
                detailFixture.getProductOption().getId(),
                detailFixture.getId()
        );
        String requestDTO = om.writeValueAsString(reviewDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "review")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "리뷰 작성. 주문 상세 데이터의 리뷰 상태값이 true인 경우")
    void postReviewOrderDetailReviewStatusIsTrue() throws Exception {
        ProductOrder fixture = productOrderList.stream()
                .filter(v ->
                        v.getMember().getUserId().equals(member.getUserId())
                )
                .findFirst()
                .get();
        fixture.setOrderStat(OrderStatus.COMPLETE.getStatusStr());
        productOrderRepository.save(fixture);

        ProductOrderDetail detailFixture = fixture.getProductOrderDetailList().stream()
                .filter(v ->
                        !v.isOrderReviewStatus()
                )
                .findFirst()
                .get();
        detailFixture.setOrderReviewStatus(true);
        productOrderDetailRepository.save(detailFixture);

        MyPagePostReviewDTO reviewDTO = new MyPagePostReviewDTO(
                detailFixture.getProduct().getId(),
                "test insert review content",
                detailFixture.getProductOption().getId(),
                detailFixture.getId()
        );
        String requestDTO = om.writeValueAsString(reviewDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "review")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "리뷰 작성. 상품 아이디가 null인 경우")
    void postReviewValidationProductIdIsNull() throws Exception {
        MyPagePostReviewDTO reviewDTO = new MyPagePostReviewDTO(
                null,
                "test insert review content",
                1L,
                1L
        );
        String requestDTO = om.writeValueAsString(reviewDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "review")
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
    @DisplayName(value = "리뷰 작성. 상품 아이디가 Blank인 경우")
    void postReviewValidationProductIdIsBlank() throws Exception {
        MyPagePostReviewDTO reviewDTO = new MyPagePostReviewDTO(
                "",
                "test insert review content",
                1L,
                1L
        );
        String requestDTO = om.writeValueAsString(reviewDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "review")
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
    @DisplayName(value = "리뷰 작성. 리뷰 내용이 null인 경우")
    void postReviewValidationContentIsNull() throws Exception {
        MyPagePostReviewDTO reviewDTO = new MyPagePostReviewDTO(
                "validationProductId",
                null,
                1L,
                1L
        );
        String requestDTO = om.writeValueAsString(reviewDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "review")
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
    @DisplayName(value = "리뷰 작성. 리뷰 내용이 Blank인 경우")
    void postReviewValidationContentIsBlank() throws Exception {
        MyPagePostReviewDTO reviewDTO = new MyPagePostReviewDTO(
                "validationProductId",
                "",
                1L,
                1L
        );
        String requestDTO = om.writeValueAsString(reviewDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "review")
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

        List<String> contentConstraintValidationList = List.of("NotBlank", "Size");

        response.errors().forEach(v -> {
            assertEquals("content", v.field());
            assertTrue(contentConstraintValidationList.contains(v.constraint()));
        });
    }

    @Test
    @DisplayName(value = "리뷰 작성. 리뷰 내용이 한글자인 경우")
    void postReviewValidationContentLength1() throws Exception {
        MyPagePostReviewDTO reviewDTO = new MyPagePostReviewDTO(
                "validationProductId",
                "a",
                1L,
                1L
        );
        String requestDTO = om.writeValueAsString(reviewDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "review")
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
    @DisplayName(value = "리뷰 작성. 옵션 아이디가 1보다 작은 경우")
    void postReviewValidationOptionIdLT1() throws Exception {
        MyPagePostReviewDTO reviewDTO = new MyPagePostReviewDTO(
                "validationProductId",
                "Validation test content",
                0L,
                1L
        );
        String requestDTO = om.writeValueAsString(reviewDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "review")
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

        assertEquals("optionId", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "리뷰 작성. 주문 상세 아이디가 1보다 작은 경우")
    void postReviewValidationDetailIdLT1() throws Exception {
        MyPagePostReviewDTO reviewDTO = new MyPagePostReviewDTO(
                "validationProductId",
                "Validation test content",
                1L,
                0L
        );
        String requestDTO = om.writeValueAsString(reviewDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "review")
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

        assertEquals("detailId", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "리뷰 작성. 모든 필드값이 유효성 검사를 통과하지 못하는 경우")
    void postReviewValidationAllParameterIsWrong() throws Exception {
        MyPagePostReviewDTO reviewDTO = new MyPagePostReviewDTO(
                null,
                null,
                0L,
                0L
        );
        String requestDTO = om.writeValueAsString(reviewDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "review")
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

        assertEquals(4, response.errors().size());

        Map<String, String> validationMap = new HashMap<>();
        validationMap.put("productId", "NotBlank");
        validationMap.put("content", "NotBlank");
        validationMap.put("optionId", "Min");
        validationMap.put("detailId", "Min");

        response.errors().forEach(v -> {
            String constraint = validationMap.getOrDefault(v.field(), null);

            assertNotNull(constraint);
            assertEquals(constraint, v.constraint());
        });
    }

    @Test
    @DisplayName(value = "리뷰 수정")
    void patchReview() throws Exception {
        ProductReview fixture = allReviewList.stream()
                .filter(v ->
                        v.getMember().getUserId().equals(member.getUserId())
                )
                .findFirst()
                .get();
        MyPagePatchReviewDTO patchDTO = new MyPagePatchReviewDTO(fixture.getId(), "test modify review content");
        String requestDTO = om.writeValueAsString(patchDTO);
        mockMvc.perform(patch(URL_PREFIX + "review")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isNoContent())
                .andReturn();

        ProductReview patchData = productReviewRepository.findById(fixture.getId()).orElse(null);
        assertNotNull(patchData);
        assertEquals(patchDTO.content(), patchData.getReviewContent());
    }

    @Test
    @DisplayName(value = "리뷰 수정. 데이터가 없는 경우")
    void patchReviewNotFound() throws Exception {
        long wrongReviewId = getWrongReviewId();
        MyPagePatchReviewDTO patchDTO = new MyPagePatchReviewDTO(wrongReviewId, "test modify review content");
        String requestDTO = om.writeValueAsString(patchDTO);
        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "review")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();

        // findById를 통한 조회로 orElse IllegalArgumentException 발생
        Exception ex = result.getResolvedException();
        assertInstanceOf(IllegalArgumentException.class, ex);

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "리뷰 수정. 작성자가 일치하지 않는 경우")
    void patchReviewWriterNotEquals() throws Exception {
        MyPagePatchReviewDTO patchDTO = new MyPagePatchReviewDTO(noneMemberReview.getId(), "test modify review content");
        String requestDTO = om.writeValueAsString(patchDTO);
        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "review")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isForbidden())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.FORBIDDEN.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "리뷰 수정. 리뷰 아이디가 1보다 작은 경우")
    void patchReviewValidationReviewIdIsZero() throws Exception {
        MyPagePatchReviewDTO patchDTO = new MyPagePatchReviewDTO(
                0L,
                "test modify review content"
        );
        String requestDTO = om.writeValueAsString(patchDTO);
        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "review")
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

        assertEquals("reviewId", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "리뷰 수정. 리뷰 내용이 null인 경우")
    void patchReviewValidationContentIsNull() throws Exception {
        MyPagePatchReviewDTO patchDTO = new MyPagePatchReviewDTO(
                1L,
                null
        );
        String requestDTO = om.writeValueAsString(patchDTO);
        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "review")
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
    @DisplayName(value = "리뷰 수정. 리뷰 내용이 Blank인 경우")
    void patchReviewValidationContentIsBlank() throws Exception {
        MyPagePatchReviewDTO patchDTO = new MyPagePatchReviewDTO(
                1L,
                ""
        );
        String requestDTO = om.writeValueAsString(patchDTO);
        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "review")
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

        List<String> contentConstraintValidationList = List.of("NotBlank", "Size");

        response.errors().forEach(v -> {
            assertEquals("content", v.field());
            assertTrue(contentConstraintValidationList.contains(v.constraint()));
        });
    }

    @Test
    @DisplayName(value = "리뷰 수정. 리뷰 내용이 한글자인 경우")
    void patchReviewValidationContentLength1() throws Exception {
        MyPagePatchReviewDTO patchDTO = new MyPagePatchReviewDTO(
                1L,
                "a"
        );
        String requestDTO = om.writeValueAsString(patchDTO);
        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "review")
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
    @DisplayName(value = "리뷰 수정. 리뷰 아이디가 0, 내용이 한글자인 경우")
    void patchReviewValidationReviewIdIsZeroAndContentLength1() throws Exception {
        MyPagePatchReviewDTO patchDTO = new MyPagePatchReviewDTO(
                0L,
                "a"
        );
        String requestDTO = om.writeValueAsString(patchDTO);
        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "review")
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
        validationMap.put("reviewId", "Min");
        validationMap.put("content", "Size");

        response.errors().forEach(v -> {
            String constraint = validationMap.getOrDefault(v.field(), null);

            assertNotNull(constraint);
            assertEquals(constraint, v.constraint());
        });
    }

    @Test
    @DisplayName(value = "리뷰 삭제")
    void deleteReview() throws Exception {
        long deleteId = allReviewList.stream()
                .filter(v ->
                        v.getMember().getUserId().equals(member.getUserId())
                )
                .findFirst()
                .get()
                .getId();
        mockMvc.perform(delete(URL_PREFIX + "review/" + deleteId)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isNoContent())
                .andReturn();

        ProductReview deleteReview = productReviewRepository.findById(deleteId).orElse(null);
        assertNull(deleteReview);
    }

    @Test
    @DisplayName(value = "리뷰 삭제. 데이터가 없는 경우")
    void deleteReviewNotFound() throws Exception {
        long wrongReviewId = getWrongReviewId();
        MvcResult result = mockMvc.perform(delete(URL_PREFIX + "review/" + wrongReviewId)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // findById를 통한 조회로 orElse IllegalArgumentException 발생
        Exception ex = result.getResolvedException();
        assertInstanceOf(IllegalArgumentException.class, ex);

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "리뷰 삭제. 작성자가 일치하지 않는 경우")
    void deleteReviewWriterNotEquals() throws Exception {
        MvcResult result = mockMvc.perform(delete(URL_PREFIX + "review/" + noneMemberReview.getId())
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isForbidden())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.FORBIDDEN.getMessage(), response.errorMessage());

        ProductReview checkData = productReviewRepository.findById(noneMemberReview.getId()).orElse(null);
        assertNotNull(checkData);
    }

    @Test
    @DisplayName(value = "리뷰 삭제. 리뷰 아이디가 1보다 작은 경우")
    void deleteReviewValidationReviewIdIsZero() throws Exception {
        MvcResult result = mockMvc.perform(delete(URL_PREFIX + "review/0")
                                    .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                    .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                    .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                            .andExpect(status().isBadRequest())
                            .andReturn();

        // @Min에 의한 HandlerMethodValidationException 검증을 위함
        Exception ex = result.getResolvedException();
        assertInstanceOf(HandlerMethodValidationException.class, ex);

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
        assertNull(response.errors());
    }

    @Test
    @DisplayName(value = "회원 정보 수정을 위한 데이터 조회")
    void getInfo() throws Exception {
        String[] splitMail = member.getUserEmail().split("@");
        String mailSuffix = splitMail[1].substring(0, splitMail[1].indexOf('.'));
        String type = MailSuffix.findSuffixType(mailSuffix);
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "info")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        MyPageInfoDTO response = om.readValue(
                content,
                new TypeReference<>(){}
        );
        assertNotNull(response);

        assertEquals(member.getNickname(), response.nickname());
        assertEquals(member.getPhone().replaceAll("-", ""), response.phone());
        assertEquals(splitMail[0], response.mailPrefix());
        assertEquals(splitMail[1], response.mailSuffix());
        assertEquals(type, response.mailType());
    }

    @Test
    @DisplayName(value = "회원 정보 수정")
    void patchInfo() throws Exception {
        MyPageInfoPatchDTO patchDTO = new MyPageInfoPatchDTO(
                "modifyNickname",
                "01098981212",
                "modify@modify.com"
        );
        String requestDTO = om.writeValueAsString(patchDTO);

        mockMvc.perform(patch(URL_PREFIX + "info")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isNoContent())
                .andReturn();

        Member patchMember = memberRepository.findByUserId(member.getUserId());
        assertNotNull(patchMember);
        assertEquals(patchDTO.nickname(), patchMember.getNickname());
        assertEquals(patchDTO.phone(), patchMember.getPhone().replaceAll("-", ""));
        assertEquals(patchDTO.mail(), patchMember.getUserEmail());
    }

    @Test
    @DisplayName(value = "회원 정보 수정. nickname이 패턴에 맞지 않는 경우")
    void patchInfoValidationNickNamePatternNotMatch() throws Exception {
        MyPageInfoPatchDTO patchDTO = new MyPageInfoPatchDTO(
                "a",
                "01098981212",
                "modify@modify.com"
        );
        String requestDTO = om.writeValueAsString(patchDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "info")
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestDTO))
                        .andExpect(status().isBadRequest())
                        .andReturn();

        // Validator를 통한 Exception 발생임을 검증
        Exception ex = result.getResolvedException();
        assertInstanceOf(InvalidJoinPolicyException.class, ex);

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원 정보 수정. 연락처가 패턴에 맞지 않는 경우")
    void patchInfoValidationPhonePatternNotMatch() throws Exception {
        MyPageInfoPatchDTO patchDTO = new MyPageInfoPatchDTO(
                "patchNickname",
                "010-9898-1212",
                "modify@modify.com"
        );
        String requestDTO = om.writeValueAsString(patchDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "info")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Validator를 통한 Exception 발생임을 검증
        Exception ex = result.getResolvedException();
        assertInstanceOf(InvalidJoinPolicyException.class, ex);

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원 정보 수정. 이메일이 패턴에 맞지 않는 경우")
    void patchInfoValidationEmailPatternNotMatch() throws Exception {
        MyPageInfoPatchDTO patchDTO = new MyPageInfoPatchDTO(
                "patchNickname",
                "01098981212",
                "modifymodify.com"
        );
        String requestDTO = om.writeValueAsString(patchDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "info")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Validator를 통한 Exception 발생임을 검증
        Exception ex = result.getResolvedException();
        assertInstanceOf(InvalidJoinPolicyException.class, ex);

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "알림 리스트 조회. 페이지값이 1보다 작은 경우")
    void getNotificationValidationPageIsZero() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "notification")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("page", "0"))
                .andExpect(status().isBadRequest())
                .andReturn();

        // @Min에 의한 HandlerMethodValidationException 검증을 위함
        Exception ex = result.getResolvedException();
        assertInstanceOf(HandlerMethodValidationException.class, ex);

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
        assertNull(response.errors());
    }
}
