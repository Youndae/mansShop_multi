package com.example.moduleapi.controller.admin;

import com.example.moduleapi.ModuleApiApplication;
import com.example.moduleapi.config.exception.ExceptionEntity;
import com.example.moduleapi.config.exception.ValidationError;
import com.example.moduleapi.config.exception.ValidationExceptionEntity;
import com.example.moduleapi.fixture.TokenFixture;
import com.example.moduleapi.model.request.admin.qna.ClassificationRequestDTO;
import com.example.moduleapi.model.response.PagingResponseDTO;
import com.example.modulecommon.customException.CustomNotFoundException;
import com.example.modulecommon.fixture.*;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.dto.admin.qna.out.AdminQnAListResponseDTO;
import com.example.modulecommon.model.dto.page.AdminQnAPageDTO;
import com.example.modulecommon.model.dto.qna.in.QnAReplyInsertDTO;
import com.example.modulecommon.model.dto.qna.in.QnAReplyPatchDTO;
import com.example.modulecommon.model.dto.qna.out.QnADetailReplyDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.*;
import com.example.modulecommon.utils.TestPaginationUtils;
import com.example.moduleconfig.properties.CookieProperties;
import com.example.moduleconfig.properties.TokenProperties;
import com.example.modulemypage.model.dto.memberQnA.out.MemberQnADetailResponseDTO;
import com.example.modulemypage.model.dto.memberQnA.out.QnAClassificationDTO;
import com.example.modulemypage.repository.MemberQnAReplyRepository;
import com.example.modulemypage.repository.MemberQnARepository;
import com.example.modulemypage.repository.QnAClassificationRepository;
import com.example.modulenotification.repository.NotificationRepository;
import com.example.moduleproduct.model.dto.productQnA.out.ProductQnADetailResponseDTO;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import com.example.moduleproduct.repository.productQnA.ProductQnARepository;
import com.example.moduleproduct.repository.productQnAReply.ProductQnAReplyRepository;
import com.example.moduleuser.repository.AuthRepository;
import com.example.moduleuser.repository.MemberRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest(classes = ModuleApiApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class AdminQnAControllerIT {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TokenFixture tokenFixture;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RedisTemplate<String, Long> cacheRedisTemplate;

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
    private NotificationRepository notificationRepository;

    @Autowired
    private TokenProperties tokenProperties;

    @Autowired
    private CookieProperties cookieProperties;

    private Map<String, String> tokenMap;

    private String accessTokenValue;

    private String refreshTokenValue;

    private String inoValue;

    private List<ProductQnA> answerCompleteProductQnAList;

    private List<ProductQnA> newProductQnAList;

    private List<ProductQnA> allProductQnAList;

    private List<ProductQnAReply> productQnaReplyList;

    private List<QnAClassification> qnAClassificationList;

    private List<MemberQnA> answerCompleteMemberQnAList;

    private List<MemberQnA> newMemberQnAList;

    private List<MemberQnA> allMemberQnAList;

    private List<MemberQnAReply> memberQnAReplyList;

    private Member admin;

    private static final String PRODUCT_QNA_CACHING_KEY = RedisCaching.ADMIN_PRODUCT_QNA_COUNT.getKey();

    private static final String MEMBER_QNA_CACHING_KEY = RedisCaching.ADMIN_MEMBER_QNA_COUNT.getKey();

    private static final String ALL_LIST_TYPE = "all";

    private static final String NEW_LIST_TYPE = "new";

    private static final String URL_PREFIX = "/api/admin/";

    private static final ErrorCode BAD_REQUEST_ERROR_CODE = ErrorCode.BAD_REQUEST;

    @BeforeEach
    void init() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();

        MemberAndAuthFixtureDTO memberAndAuthFixtureDTO = MemberAndAuthFixture.createDefaultMember(40);
        MemberAndAuthFixtureDTO adminFixture = MemberAndAuthFixture.createAdmin();
        List<Member> memberList = memberAndAuthFixtureDTO.memberList();
        List<Member> saveMemberList = new ArrayList<>(memberList);
        saveMemberList.addAll(adminFixture.memberList());
        List<Auth> saveAuthList = new ArrayList<>(adminFixture.authList());
        saveAuthList.addAll(adminFixture.authList());
        memberRepository.saveAll(saveMemberList);
        authRepository.saveAll(saveAuthList);

        admin = adminFixture.memberList().get(0);

        tokenMap = tokenFixture.createAndSaveAllToken(admin);
        accessTokenValue = tokenMap.get(tokenProperties.getAccess().getHeader());
        refreshTokenValue = tokenMap.get(tokenProperties.getRefresh().getHeader());
        inoValue = tokenMap.get(cookieProperties.getIno().getHeader());

        List<Classification> classificationList = ClassificationFixture.createClassifications();
        classificationRepository.saveAll(classificationList);

        List<Product> productList = ProductFixture.createProductFixtureList(10, classificationList.get(0));
        List<ProductOption> productOptionList = productList.stream()
                .flatMap(v -> v.getProductOptions().stream())
                .toList();
        productRepository.saveAll(productList);
        productOptionRepository.saveAll(productOptionList);

        answerCompleteProductQnAList = ProductQnAFixture.createProductQnACompletedAnswer(memberList, productList);
        newProductQnAList = ProductQnAFixture.createDefaultProductQnA(memberList, productList);
        allProductQnAList = new ArrayList<>(answerCompleteProductQnAList);
        allProductQnAList.addAll(newProductQnAList);
        productQnARepository.saveAll(allProductQnAList);
        productQnaReplyList = ProductQnAFixture.createDefaultProductQnaReply(admin, answerCompleteProductQnAList);
        productQnAReplyRepository.saveAll(productQnaReplyList);

        qnAClassificationList = QnAClassificationFixture.createQnAClassificationList();
        qnAClassificationRepository.saveAll(qnAClassificationList);

        answerCompleteMemberQnAList = MemberQnAFixture.createMemberQnACompletedAnswer(qnAClassificationList, memberList);
        newMemberQnAList = MemberQnAFixture.createDefaultMemberQnA(qnAClassificationList, memberList);
        allMemberQnAList = new ArrayList<>(answerCompleteMemberQnAList);
        allMemberQnAList.addAll(newMemberQnAList);
        memberQnARepository.saveAll(allMemberQnAList);
        memberQnAReplyList = MemberQnAFixture.createMemberQnAReply(answerCompleteMemberQnAList, admin);
        memberQnAReplyRepository.saveAll(memberQnAReplyList);
    }

    @AfterEach
    void cleanUP() {
        notificationRepository.deleteAll();
        memberQnAReplyRepository.deleteAll();
        memberQnARepository.deleteAll();
        qnAClassificationRepository.deleteAll();
        productQnAReplyRepository.deleteAll();
        productQnARepository.deleteAll();
        productOptionRepository.deleteAll();
        productRepository.deleteAll();
        classificationRepository.deleteAll();
        authRepository.deleteAll();
        memberRepository.deleteAll();

        String accessKey = tokenMap.get("accessKey");
        String refreshKey = tokenMap.get("refreshKey");

        redisTemplate.delete(accessKey);
        redisTemplate.delete(refreshKey);

        cacheRedisTemplate.delete(PRODUCT_QNA_CACHING_KEY);
        cacheRedisTemplate.delete(MEMBER_QNA_CACHING_KEY);
    }

    private long getWrongProductQnAId() {
        long fixtureId = allProductQnAList.get(0).getId() - allProductQnAList.size() - 100;
        if(fixtureId < 1)
            fixtureId = allProductQnAList.size() + 100;

        return fixtureId;
    }

    private long getWrongMemberQnAId() {
        long fixtureId = allMemberQnAList.get(0).getId() - allMemberQnAList.size() - 100;
        if(fixtureId < 1)
            fixtureId = allMemberQnAList.size() + 100;

        return fixtureId;
    }

    @Test
    @DisplayName(value = "상품 문의 전체 목록 조회")
    void getAllProductQnA() throws Exception {
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO(null, null, 1);
        List<ProductQnA> fixtureList = allProductQnAList.stream()
                .limit(pageDTO.amount())
                .toList();
        int contentSize = Math.min(fixtureList.size(), pageDTO.amount());
        int totalPages = TestPaginationUtils.getTotalPages(allProductQnAList.size(), pageDTO.amount());

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("type", ALL_LIST_TYPE))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminQnAListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(contentSize, response.content().size());
        assertEquals(totalPages, response.totalPages());

        Long cachingCount = cacheRedisTemplate.opsForValue().get(PRODUCT_QNA_CACHING_KEY);
        assertNotNull(cachingCount);
        assertEquals(allProductQnAList.size(), cachingCount);
    }

    @Test
    @DisplayName(value = "상품 문의 미처리 목록 조회")
    void getNewProductQnA() throws Exception {
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO(null, null, 1);
        List<ProductQnA> fixtureList = newProductQnAList.stream()
                .limit(pageDTO.amount())
                .toList();
        int contentSize = Math.min(fixtureList.size(), pageDTO.amount());
        int totalPages = TestPaginationUtils.getTotalPages(newProductQnAList.size(), pageDTO.amount());

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("type", NEW_LIST_TYPE))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminQnAListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(contentSize, response.content().size());
        assertEquals(totalPages, response.totalPages());

        Long cachingCount = cacheRedisTemplate.opsForValue().get(PRODUCT_QNA_CACHING_KEY);
        assertNull(cachingCount);
    }

    @Test
    @DisplayName(value = "상품 문의 전체 목록 조회. 아이디 또는 닉네임 기반 검색")
    void getAllProductQnASearch() throws Exception {
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO(null, null, 1);
        ProductQnA fixture = allProductQnAList.get(0);
        List<ProductQnA> fixtureList = allProductQnAList.stream()
                .filter(v ->
                        v.getMember().getUserId().contains(fixture.getMember().getUserId()) ||
                                v.getMember().getNickname().contains(fixture.getMember().getUserId())
                )
                .toList();
        List<ProductQnA> fixtureContentList = fixtureList.stream().limit(pageDTO.amount()).toList();
        int contentSize = Math.min(fixtureContentList.size(), pageDTO.amount());
        int totalPages = TestPaginationUtils.getTotalPages(fixtureList.size(), pageDTO.amount());

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("type", ALL_LIST_TYPE)
                        .param("keyword", fixture.getMember().getUserId()))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminQnAListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(contentSize, response.content().size());
        assertEquals(totalPages, response.totalPages());

        Long cachingCount = cacheRedisTemplate.opsForValue().get(PRODUCT_QNA_CACHING_KEY);
        assertNull(cachingCount);
    }

    @Test
    @DisplayName(value = "상품 문의 전체 목록 조회. 데이터가 없는 경우")
    void getAllProductQnAEmpty() throws Exception {
        productQnARepository.deleteAll();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("type", ALL_LIST_TYPE))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminQnAListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertTrue(response.empty());
        assertEquals(0, response.totalPages());

        Long cachingCount = cacheRedisTemplate.opsForValue().get(PRODUCT_QNA_CACHING_KEY);
        assertEquals(0, cachingCount);
    }

    @Test
    @DisplayName(value = "상품 문의 미처리 목록 조회. 데이터가 없는 경우")
    void getNewProductQnAEmpty() throws Exception {
        productQnARepository.deleteAll();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("type", NEW_LIST_TYPE))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminQnAListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertTrue(response.empty());
        assertEquals(0, response.totalPages());

        Long cachingCount = cacheRedisTemplate.opsForValue().get(PRODUCT_QNA_CACHING_KEY);
        assertNull(cachingCount);
    }

    @Test
    @DisplayName(value = "상품 문의 전체 목록 조회. 페이지값이 0인 경우")
    void getAllProductQnAValidationPageZero() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("page", "0")
                        .param("type", ALL_LIST_TYPE))
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("page", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 문의 전체 목록 조회. 검색어가 한글자인 경우")
    void getAllProductQnAValidationKeywordLength1() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("keyword", "a")
                        .param("type", ALL_LIST_TYPE))
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("keyword", responseObj.field());
        assertEquals("Size", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 문의 전체 목록 조회. 페이지값이 0, keyword가 한글자인 경우")
    void getAllProductQnAValidationPageZeroAndKeywordLength1() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("page", "0")
                        .param("keyword", "a")
                        .param("type", ALL_LIST_TYPE))
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(2, response.errors().size());

        Map<String, String> validationMap = new HashMap<>();
        validationMap.put("page", "Min");
        validationMap.put("keyword", "Size");

        response.errors().forEach(v -> {
            String constraint = validationMap.getOrDefault(v.field(), null);

            assertNotNull(constraint);
            assertEquals(constraint, v.constraint());
        });
    }

    @Test
    @DisplayName(value = "상품 문의 전체 목록 조회. listType이 null인 경우")
    void getAllProductQnAValidationListTypeIsNull() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    @DisplayName(value = "상품 문의 전체 목록 조회. listType이 Blank인 경우")
    void getAllProductQnAValidationListTypeIsBlank() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("type", ""))
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();

        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertNull(response.errors());
    }

    @Test
    @DisplayName(value = "상품 문의 전체 목록 조회. listType이 잘못된 경우")
    void getAllProductQnAValidationWrongListType() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("type", "abc"))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(
                IllegalArgumentException.class.getSimpleName(),
                result.getResolvedException().getClass().getSimpleName()
        );

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "상품 문의 전체 목록 조회. page값이 0, keyword 한글자, listType이 잘못된 경우")
    void getAllProductQnAValidationAllParameterInvalid() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("page", "0")
                        .param("keyword", "a")
                        .param("type", "abc"))
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();

        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(2, response.errors().size());

        Map<String, String> validationMap = new HashMap<>();
        validationMap.put("page", "Min");
        validationMap.put("keyword", "Size");

        response.errors().forEach(v -> {
            String constraint = validationMap.getOrDefault(v.field(), null);

            assertNotNull(constraint);
            assertEquals(constraint, v.constraint());
        });
    }

    @Test
    @DisplayName(value = "답변 완료 된 상품 문의 상세 조회")
    void getAnswerProductQnADetail() throws Exception {
        ProductQnA fixture = answerCompleteProductQnAList.get(0);
        ProductQnAReply fixtureReply = productQnaReplyList.stream()
                .filter(v ->
                        v.getProductQnA().getId().equals(fixture.getId())
                )
                .toList()
                .get(0);

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product/" + fixture.getId())
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ProductQnADetailResponseDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(fixture.getId(), response.productQnAId());
        assertEquals(fixture.getProduct().getProductName(), response.productName());
        assertEquals(fixture.getMember().getNickname(), response.writer());
        assertEquals(fixture.getQnaContent(), response.qnaContent());
        assertEquals(fixture.isProductQnAStat(), response.productQnAStat());
        assertEquals(1, response.replyList().size());

        QnADetailReplyDTO replyResponse = response.replyList().get(0);
        assertEquals(fixtureReply.getId(), replyResponse.replyId());
        assertEquals(fixtureReply.getMember().getNickname(), replyResponse.writer());
        assertEquals(fixtureReply.getReplyContent(), replyResponse.replyContent());
    }

    @Test
    @DisplayName(value = "미답변 상품 문의 상세 조회")
    void getNewProductQnADetail() throws Exception {
        ProductQnA fixture = newProductQnAList.get(0);

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product/" + fixture.getId())
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ProductQnADetailResponseDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(fixture.getId(), response.productQnAId());
        assertEquals(fixture.getProduct().getProductName(), response.productName());
        assertEquals(fixture.getMember().getNickname(), response.writer());
        assertEquals(fixture.getQnaContent(), response.qnaContent());
        assertEquals(fixture.isProductQnAStat(), response.productQnAStat());
        assertTrue(response.replyList().isEmpty());
    }

    @Test
    @DisplayName(value = "상품 문의 상세 조회. 상품 문의 아이디가 잘못된 경우")
    void getProductQnADetailWrongId() throws Exception {
        long fixtureId = getWrongProductQnAId();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product/" + fixtureId)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(
                CustomNotFoundException.class.getSimpleName(),
                result.getResolvedException().getClass().getSimpleName()
        );

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "상품 문의 상세 조회. 상품 문의 아이디가 1보다 작은 경우")
    void getProductQnADetailIdLT1() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/product/0")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(
                HandlerMethodValidationException.class.getSimpleName(),
                result.getResolvedException().getClass().getSimpleName()
        );

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertNull(response.errors());
    }

    @Test
    @DisplayName(value = "상품 문의 답변 상태를 완료로 수정")
    void patchProductQnAComplete() throws Exception {
        ProductQnA fixture = newProductQnAList.get(0);

        mockMvc.perform(patch(URL_PREFIX + "qna/product/" + fixture.getId())
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isNoContent())
                .andReturn();

        ProductQnA patchData = productQnARepository.findById(fixture.getId()).orElse(null);
        assertNotNull(patchData);
        assertTrue(patchData.isProductQnAStat());
    }

    @Test
    @DisplayName(value = "상품 문의 답변 상태를 완료로 수정. 문의 아이디가 잘못된 경우")
    void patchProductQnACompleteWrongId() throws Exception {
        long fixtureId = getWrongProductQnAId();

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/product/" + fixtureId)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(
                IllegalArgumentException.class.getSimpleName(),
                result.getResolvedException().getClass().getSimpleName()
        );

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "상품 문의 답변 상태를 완료로 수정. 상품 문의 아이디가 1보다 작은 경우")
    void patchProductQnACompleteIdLT1() throws Exception {
        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/product/0")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(
                HandlerMethodValidationException.class.getSimpleName(),
                result.getResolvedException().getClass().getSimpleName()
        );

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertNull(response.errors());
    }

    @Test
    @DisplayName(value = "상품 문의 답변 작성.")
    void postProductQnAReply() throws Exception {
        ProductQnA fixture = newProductQnAList.get(0);
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(fixture.getId(), "test insert productQnA Reply content");
        String requestDTO = om.writeValueAsString(insertDTO);

        mockMvc.perform(post(URL_PREFIX + "qna/product/reply")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestDTO))
                .andExpect(status().isNoContent())
                .andReturn();

        List<QnADetailReplyDTO> saveReplyList = productQnAReplyRepository.findAllByQnAId(fixture.getId());
        assertFalse(saveReplyList.isEmpty());
        assertEquals(1, saveReplyList.size());

        QnADetailReplyDTO replyResponse = saveReplyList.get(0);

        assertEquals(admin.getNickname(), replyResponse.writer());
        assertEquals(insertDTO.content(), replyResponse.replyContent());

        await()
                .atMost(20, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    List<Notification> notificationList = notificationRepository.findAll();
                    assertFalse(notificationList.isEmpty());
                    assertEquals(1, notificationList.size());
                    Notification notification = notificationList.get(0);
                    String notificationTitle = fixture.getProduct().getProductName() + NotificationType.PRODUCT_QNA_REPLY.getTitle();
                    assertEquals(fixture.getMember().getUserId(), notification.getMember().getUserId());
                    assertEquals(notificationTitle, notification.getTitle());
                    assertEquals(fixture.getId(), notification.getRelatedId());
                });
    }

    @Test
    @DisplayName(value = "상품 문의 답변 작성. 문의 아이디가 잘못된 경우")
    void postProductQnAReplyWrongId() throws Exception {
        long fixtureId = getWrongProductQnAId();
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(fixtureId, "test insert productQnA Reply content");
        String requestDTO = om.writeValueAsString(insertDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna/product/reply")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(
                IllegalArgumentException.class.getSimpleName(),
                result.getResolvedException().getClass().getSimpleName()
        );

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "상품 문의 답변 작성. 문의 아이디가 1보다 작은 경우")
    void postProductQnAReplyValidationQnAIdLT1() throws Exception {
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(0L, "test insert productQnA Reply content");
        String requestDTO = om.writeValueAsString(insertDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna/product/reply")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("qnaId", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 문의 답변 작성. 답변 내용이 null인 경우")
    void postProductQnAReplyValidationContentIsNull() throws Exception {
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(1L, null);
        String requestDTO = om.writeValueAsString(insertDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna/product/reply")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("content", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 문의 답변 작성. 문의 내용이 Blank인 경우")
    void postProductQnAReplyValidationContentIsBlank() throws Exception {
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(1L, "");
        String requestDTO = om.writeValueAsString(insertDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna/product/reply")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("content", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 문의 답변 작성. 문의 아이디가 0, 내용이 Blank인 경우")
    void postProductQnAReplyValidationQnAIdLT1AndContentIsBlank() throws Exception {
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(0L, "");
        String requestDTO = om.writeValueAsString(insertDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna/product/reply")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
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
    @DisplayName(value = "상품 문의 답변 수정")
    void patchProductQnAReply() throws Exception {
        ProductQnAReply fixture = productQnaReplyList.get(0);
        QnAReplyPatchDTO replyDTO = new QnAReplyPatchDTO(fixture.getId(), "test patch productQnA Reply content");
        String requestDTO = om.writeValueAsString(replyDTO);

        mockMvc.perform(patch(URL_PREFIX + "qna/product/reply")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestDTO))
                .andExpect(status().isNoContent())
                .andReturn();

        ProductQnAReply patchData = productQnAReplyRepository.findById(fixture.getId()).orElse(null);
        assertNotNull(patchData);
        assertEquals(replyDTO.content(), patchData.getReplyContent());
    }

    @Test
    @DisplayName(value = "상품 문의 답변 수정. 답변 아이디가 잘못된 경우")
    void patchProductQnAReplyWrongId() throws Exception {
        long fixtureId = getWrongProductQnAId();
        QnAReplyPatchDTO replyDTO = new QnAReplyPatchDTO(fixtureId, "test patch productQnA Reply content");
        String requestDTO = om.writeValueAsString(replyDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/product/reply")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(
                IllegalArgumentException.class.getSimpleName(),
                result.getResolvedException().getClass().getSimpleName()
        );

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "상품 문의 답변 수정. 답변 아이디가 1보다 작은 경우")
    void patchProductQnAReplyValidationReplyIdLT1() throws Exception {
        QnAReplyPatchDTO replyDTO = new QnAReplyPatchDTO(0L, "test patch productQnA Reply content");
        String requestDTO = om.writeValueAsString(replyDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/product/reply")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("replyId", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 문의 답변 수정. 답변 내용이 null인 경우")
    void patchProductQnAReplyValidationContentIsNull() throws Exception {
        QnAReplyPatchDTO replyDTO = new QnAReplyPatchDTO(1L, null);
        String requestDTO = om.writeValueAsString(replyDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/product/reply")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("content", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 문의 답변 수정. 답변 내용이 Blank인 경우")
    void patchProductQnAReplyValidationContentIsBlank() throws Exception {
        QnAReplyPatchDTO replyDTO = new QnAReplyPatchDTO(1L, "");
        String requestDTO = om.writeValueAsString(replyDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/product/reply")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("content", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 문의 답변 수정. 답변 아이디가 0, 답변 내용이 Blank인 경우")
    void patchProductQnAReplyValidationReplyIdLT1AndContentIsBlank() throws Exception {
        QnAReplyPatchDTO replyDTO = new QnAReplyPatchDTO(0L, "");
        String requestDTO = om.writeValueAsString(replyDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/product/reply")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
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
    @DisplayName(value = "전체 회원 문의 목록 조회")
    void getAllMemberQnAList() throws Exception {
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO(null, null, 1);
        List<MemberQnA> fixtureList = allMemberQnAList.stream()
                .limit(pageDTO.amount())
                .toList();
        int contentSize = Math.min(fixtureList.size(), pageDTO.amount());
        int totalPages = TestPaginationUtils.getTotalPages(allMemberQnAList.size(), pageDTO.amount());

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("type", ALL_LIST_TYPE))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminQnAListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(contentSize, response.content().size());
        assertEquals(totalPages, response.totalPages());

        Long cachingCount = cacheRedisTemplate.opsForValue().get(MEMBER_QNA_CACHING_KEY);
        assertNotNull(cachingCount);
        assertEquals(allMemberQnAList.size(), cachingCount);
    }

    @Test
    @DisplayName(value = "미처리 회원 문의 목록 조회")
    void getNewMemberQnAList() throws Exception {
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO(null, null, 1);
        List<MemberQnA> fixtureList = newMemberQnAList.stream()
                .limit(pageDTO.amount())
                .toList();
        int contentSize = Math.min(fixtureList.size(), pageDTO.amount());
        int totalPages = TestPaginationUtils.getTotalPages(newMemberQnAList.size(), pageDTO.amount());

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("type", NEW_LIST_TYPE))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminQnAListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(contentSize, response.content().size());
        assertEquals(totalPages, response.totalPages());

        Long cachingCount = cacheRedisTemplate.opsForValue().get(MEMBER_QNA_CACHING_KEY);
        assertNull(cachingCount);
    }

    @Test
    @DisplayName(value = "전체 회원 문의 목록 조회. 아이디 또는 닉네임 기반 검색")
    void getAllMemberQnAListSearch() throws Exception {
        MemberQnA fixture = allMemberQnAList.get(0);
        AdminQnAPageDTO pageDTO = new AdminQnAPageDTO(null, 1);
        List<MemberQnA> fixtureList = allMemberQnAList.stream()
                .filter(v ->
                        v.getMember().getUserId().contains(fixture.getMember().getUserId()) ||
                                v.getMember().getNickname().contains(fixture.getMember().getUserId())
                )
                .toList();
        List<MemberQnA> fixtureContentList = fixtureList.stream().limit(pageDTO.amount()).toList();
        int contentSize = Math.min(fixtureContentList.size(), pageDTO.amount());
        int totalPages = TestPaginationUtils.getTotalPages(fixtureList.size(), pageDTO.amount());

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("type", ALL_LIST_TYPE)
                        .param("keyword", fixture.getMember().getUserId()))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminQnAListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(contentSize, response.content().size());
        assertEquals(totalPages, response.totalPages());

        Long cachingCount = cacheRedisTemplate.opsForValue().get(MEMBER_QNA_CACHING_KEY);
        assertNull(cachingCount);
    }

    @Test
    @DisplayName(value = "회원 문의 전체 목록 조회. 데이터가 없는 경우")
    void getAllMemberQnAEmpty() throws Exception {
        memberQnARepository.deleteAll();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("type", ALL_LIST_TYPE))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminQnAListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertTrue(response.empty());
        assertEquals(0, response.totalPages());

        Long cachingCount = cacheRedisTemplate.opsForValue().get(MEMBER_QNA_CACHING_KEY);
        assertEquals(0, cachingCount);
    }

    @Test
    @DisplayName(value = "회원 문의 미처리 목록 조회. 데이터가 없는 경우")
    void getNewMemberQnAEmpty() throws Exception {
        memberQnARepository.deleteAll();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("type", NEW_LIST_TYPE))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminQnAListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertTrue(response.empty());
        assertEquals(0, response.totalPages());

        Long cachingCount = cacheRedisTemplate.opsForValue().get(MEMBER_QNA_CACHING_KEY);
        assertNull(cachingCount);
    }

    @Test
    @DisplayName(value = "회원 문의 목록 조회. 페이지값이 0인 경우")
    void getAllMemberQnAListValidationPageZero() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("page", "0")
                        .param("type", ALL_LIST_TYPE))
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("page", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원 문의 목록 조회. 검색어가 한글자인 경우")
    void getAllMemberQnAListValidationKeywordLength1() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("keyword", "a")
                        .param("type", ALL_LIST_TYPE))
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("keyword", responseObj.field());
        assertEquals("Size", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원 문의 목록 조회. 검색어가 한글자인 경우")
    void getAllMemberQnAListValidationPageZeroAndKeywordLength1() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("page", "0")
                        .param("keyword", "a")
                        .param("type", ALL_LIST_TYPE))
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(2, response.errors().size());

        Map<String, String> validationMap = new HashMap<>();
        validationMap.put("page", "Min");
        validationMap.put("keyword", "Size");

        response.errors().forEach(v -> {
            String constraint = validationMap.getOrDefault(v.field(), null);

            assertNotNull(constraint);
            assertEquals(constraint, v.constraint());
        });
    }

    @Test
    @DisplayName(value = "회원 문의 목록 조회. listType이 null인 경우")
    void getAllMemberQnAListValidationListTypeIsNull() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    @DisplayName(value = "회원 문의 목록 조회. listType이 Blank인 경우")
    void getAllMemberQnAListValidationListTypeIsBlank() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("type", ""))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(
                HandlerMethodValidationException.class.getSimpleName(),
                result.getResolvedException().getClass().getSimpleName()
        );

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertNull(response.errors());
    }

    @Test
    @DisplayName(value = "회원 문의 목록 조회. listType이 잘못된 경우")
    void getAllMemberQnAListValidationWrongListType() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("type", "abc"))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertEquals(
                IllegalArgumentException.class.getSimpleName(),
                result.getResolvedException().getClass().getSimpleName()
        );

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원 문의 목록 조회. page값이 0, keyword 한글자, listType이 잘못된 경우")
    void getAllMemberQnAListValidationAllParameterInvalid() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("page", "0")
                        .param("keyword", "a")
                        .param("type", "abc"))
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(2, response.errors().size());

        Map<String, String> validationMap = new HashMap<>();
        validationMap.put("page", "Min");
        validationMap.put("keyword", "Size");

        response.errors().forEach(v -> {
            String constraint = validationMap.getOrDefault(v.field(), null);

            assertNotNull(constraint);
            assertEquals(constraint, v.constraint());
        });
    }

    @Test
    @DisplayName(value = "답변이 있는 회원 문의 상세 조회")
    void getAnswerMemberQnADetail() throws Exception {
        MemberQnA fixture = answerCompleteMemberQnAList.get(0);
        List<QnADetailReplyDTO> replyFixtureList = memberQnAReplyList.stream()
                .filter(v ->
                        v.getMemberQnA().getId().equals(fixture.getId())
                )
                .map(v ->
                        new QnADetailReplyDTO(
                                v.getId(),
                                v.getMember().getNickname(),
                                v.getReplyContent(),
                                v.getUpdatedAt()
                        )
                )
                .toList();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member/" + fixture.getId())
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        MemberQnADetailResponseDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(fixture.getId(), response.memberQnAId());
        assertEquals(fixture.getQnAClassification().getQnaClassificationName(), response.qnaClassification());
        assertEquals(fixture.getMemberQnATitle(), response.qnaTitle());
        assertEquals(fixture.getMember().getNickname(), response.writer());
        assertEquals(fixture.getMemberQnAContent(), response.qnaContent());
        assertEquals(fixture.isMemberQnAStat(), response.memberQnAStat());
        assertEquals(replyFixtureList.size(), response.replyList().size());

        replyFixtureList.forEach(v -> assertTrue(response.replyList().contains(v)));
    }

    @Test
    @DisplayName(value = "답변이 없는 회원 문의 상세 조회")
    void getNewMemberQnADetail() throws Exception {
        MemberQnA fixture = newMemberQnAList.get(0);

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member/" + fixture.getId())
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        MemberQnADetailResponseDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(fixture.getId(), response.memberQnAId());
        assertEquals(fixture.getQnAClassification().getQnaClassificationName(), response.qnaClassification());
        assertEquals(fixture.getMemberQnATitle(), response.qnaTitle());
        assertEquals(fixture.getMember().getNickname(), response.writer());
        assertEquals(fixture.getMemberQnAContent(), response.qnaContent());
        assertEquals(fixture.isMemberQnAStat(), response.memberQnAStat());
        assertTrue(response.replyList().isEmpty());
    }

    @Test
    @DisplayName(value = "회원 문의 상세 조회. 문의 아이디가 잘못된 경우")
    void getMemberQnADetailWrongId() throws Exception {
        long fixtureId = getWrongMemberQnAId();
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member/" + fixtureId)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(
                CustomNotFoundException.class.getSimpleName(),
                result.getResolvedException().getClass().getSimpleName()
        );

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원 문의 상세 조회. 문의 아이디가 1보다 작은 경우")
    void getMemberQnADetailIdLT1() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/member/0")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertEquals(
                HandlerMethodValidationException.class.getSimpleName(),
                result.getResolvedException().getClass().getSimpleName()
        );

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertNull(response.errors());
    }

    @Test
    @DisplayName(value = "회원 문의 답변 완료 상태로 수정")
    void patchMemberQnAStatusComplete() throws Exception {
        MemberQnA fixture = newMemberQnAList.get(0);

        mockMvc.perform(patch(URL_PREFIX + "qna/member/" + fixture.getId())
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isNoContent())
                .andReturn();

        MemberQnA patchData = memberQnARepository.findById(fixture.getId()).orElse(null);
        assertNotNull(patchData);
        assertTrue(patchData.isMemberQnAStat());
    }

    @Test
    @DisplayName(value = "회원 문의 답변 완료 상태로 수정. 문의 아이디가 잘못된 경우")
    void patchMemberQnAStatusCompleteWrongId() throws Exception {
        long fixtureId = getWrongMemberQnAId();
        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/member/" + fixtureId)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(
                IllegalArgumentException.class.getSimpleName(),
                result.getResolvedException().getClass().getSimpleName()
        );

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원 문의 답변 완료 상태로 수정. 문의 아이디가 1보다 작은 경우")
    void patchMemberQnAStatusCompleteIdLT1() throws Exception {
        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/member/0")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertEquals(
                HandlerMethodValidationException.class.getSimpleName(),
                result.getResolvedException().getClass().getSimpleName()
        );

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertNull(response.errors());
    }

    @Test
    @DisplayName(value = "회원 문의 답변 작성")
    void postMemberQnAReply() throws Exception {
        MemberQnA fixture = newMemberQnAList.get(0);
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(fixture.getId(), "test insert MemberQnA reply content");
        String requestDTO = om.writeValueAsString(insertDTO);

        mockMvc.perform(post(URL_PREFIX + "qna/member/reply")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isNoContent())
                .andReturn();

        MemberQnA patchData = memberQnARepository.findById(fixture.getId()).orElse(null);
        assertNotNull(patchData);
        assertTrue(patchData.isMemberQnAStat());

        List<QnADetailReplyDTO> insertReplyList = memberQnAReplyRepository.findAllByQnAId(fixture.getId());
        assertNotNull(insertReplyList);
        assertFalse(insertReplyList.isEmpty());
        assertEquals(1, insertReplyList.size());

        QnADetailReplyDTO replyDTO = insertReplyList.get(0);
        assertEquals(admin.getNickname(), replyDTO.writer());
        assertEquals(insertDTO.content(), replyDTO.replyContent());

        await()
                .atMost(20, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    List<Notification> notificationList = notificationRepository.findAll();
                    assertFalse(notificationList.isEmpty());
                    assertEquals(1, notificationList.size());

                    Notification notification = notificationList.get(0);
                    String notificationTitle = fixture.getMemberQnATitle() + NotificationType.MEMBER_QNA_REPLY.getTitle();
                    assertEquals(fixture.getMember().getUserId(), notification.getMember().getUserId());
                    assertEquals(notificationTitle, notification.getTitle());
                    assertEquals(fixture.getId(), notification.getRelatedId());
                });
    }

    @Test
    @DisplayName(value = "회원 문의 답변 작성. 회원 문의 아이디가 잘못된 경우")
    void postMemberQnAReplyWrongId() throws Exception {
        long fixtureId = getWrongMemberQnAId();
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(fixtureId, "test insert MemberQnA reply content");
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
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원 문의 답변 작성. 회원 문의 아이디가 1보다 작은 경우")
    void postMemberQnAReplyValidationQnAIdLT1() throws Exception {
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(0L, "test insert MemberQnA reply content");
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
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("qnaId", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원 문의 답변 작성. 답변 내용이 null인 경우")
    void postMemberQnAReplyValidationContentIsNull() throws Exception {
        MemberQnA fixture = newMemberQnAList.get(0);
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(fixture.getId(), null);
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
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("content", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원 문의 답변 작성. 답변 내용이 Blank인 경우")
    void postMemberQnAReplyValidationContentIsBlank() throws Exception {
        MemberQnA fixture = newMemberQnAList.get(0);
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(fixture.getId(), "");
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
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("content", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원 문의 답변 작성. 문의 아이디가 0, 답변 내용이 Blank인 경우")
    void postMemberQnAReplyValidationQnAIdLT1AndContentIsBlank() throws Exception {
        QnAReplyInsertDTO insertDTO = new QnAReplyInsertDTO(0L, "");
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
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
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
        MemberQnAReply fixture = memberQnAReplyList.get(0);
        QnAReplyPatchDTO replyDTO = new QnAReplyPatchDTO(fixture.getId(), "test patch MemberQnA reply content");
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
    @DisplayName(value = "회원 문의 답변 수정. 답변 아이디가 잘못된 경우")
    void patchMemberQnAReplyWrongId() throws Exception {
        long fixtureId = getWrongMemberQnAId();
        QnAReplyPatchDTO replyDTO = new QnAReplyPatchDTO(fixtureId, "test patch MemberQnA reply content");
        String requestDTO = om.writeValueAsString(replyDTO);

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "qna/member/reply")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(
                IllegalArgumentException.class.getSimpleName(),
                result.getResolvedException().getClass().getSimpleName()
        );

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원 문의 답변 수정. 답변 아이디가 1보다 작은 경우")
    void patchMemberQnAReplyValidationQnAIdLT1() throws Exception {
        QnAReplyPatchDTO replyDTO = new QnAReplyPatchDTO(0L, "test patch MemberQnA reply content");
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
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
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
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
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
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("content", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원 문의 답변 수정. 답변 아이디가 0, 답변 내용이 Blank인 경우")
    void patchMemberQnAReplyValidationQnAIdLT1AndContentIsBlank() throws Exception {
        QnAReplyPatchDTO replyDTO = new QnAReplyPatchDTO(0L, "");
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
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
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
    @DisplayName(value = "회원 문의 분류 조회")
    void getQnAClassificationList() throws Exception {
        List<QnAClassificationDTO> fixture = qnAClassificationList.stream()
                .map(v ->
                        new QnAClassificationDTO(v.getId(), v.getQnaClassificationName())
                )
                .toList();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "qna/classification")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        List<QnAClassificationDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(fixture.size(), response.size());

        fixture.forEach(v -> assertTrue(response.contains(v)));
    }

    @Test
    @DisplayName(value = "회원 문의 분류 조회. 데이터가 없는 경우")
    void getQnAClassificationListEmpty() throws Exception {
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
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    @DisplayName(value = "회원 문의 분류 추가")
    void postQnAClassification() throws Exception {
        String classification = "testClassificationName";
        ClassificationRequestDTO insertDTO = new ClassificationRequestDTO(classification);
        String requestDTO = om.writeValueAsString(insertDTO);
        int classificationSize = qnAClassificationList.size() + 1;
        mockMvc.perform(post(URL_PREFIX + "qna/classification")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isNoContent())
                .andReturn();

        List<QnAClassification> qnAClassifications = qnAClassificationRepository.findAll();
        assertNotNull(qnAClassifications);
        assertFalse(qnAClassifications.isEmpty());
        assertEquals(classificationSize, qnAClassifications.size());

        QnAClassification lastIdClassification = qnAClassifications.get(qnAClassifications.size() - 1);
        assertEquals(classification, lastIdClassification.getQnaClassificationName());
    }

    @Test
    @DisplayName(value = "회원 문의 분류 추가. 분류명이 null인 경우")
    void postQnAClassificationValidationClassificationIsNull() throws Exception {
        String classification = null;
        ClassificationRequestDTO insertDTO = new ClassificationRequestDTO(classification);
        String requestDTO = om.writeValueAsString(insertDTO);
        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna/classification")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(
                MethodArgumentNotValidException.class.getSimpleName(),
                result.getResolvedException().getClass().getSimpleName()
        );

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("classification", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원 문의 분류 추가. 분류명이 Blank인 경우")
    void postQnAClassificationValidationClassificationIsBlank() throws Exception {
        String classification = "";
        ClassificationRequestDTO insertDTO = new ClassificationRequestDTO(classification);
        String requestDTO = om.writeValueAsString(insertDTO);
        MvcResult result = mockMvc.perform(post(URL_PREFIX + "qna/classification")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(
                MethodArgumentNotValidException.class.getSimpleName(),
                result.getResolvedException().getClass().getSimpleName()
        );

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("classification", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원 문의 분류 삭제")
    void deleteQnAClassification() throws Exception {
        Long deleteId = qnAClassificationList.get(0).getId();
        int classificationSize = qnAClassificationList.size() - 1;
        mockMvc.perform(delete(URL_PREFIX + "qna/classification/" + deleteId)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isNoContent())
                .andReturn();

        List<QnAClassification> qnAClassifications = qnAClassificationRepository.findAll();
        assertNotNull(qnAClassifications);
        assertFalse(qnAClassifications.isEmpty());
        assertEquals(classificationSize, qnAClassifications.size());

        QnAClassification deleteCheck = qnAClassificationRepository.findById(deleteId).orElse(null);
        assertNull(deleteCheck);
    }

    @Test
    @DisplayName(value = "회원 문의 분류 삭제. 분류 아이디가 잘못된 경우")
    void deleteQnAClassificationWrongId() throws Exception {
        long fixtureId = qnAClassificationList.get(0).getId() + 100;
        MvcResult result = mockMvc.perform(delete(URL_PREFIX + "qna/classification/" + fixtureId)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(
                IllegalArgumentException.class.getSimpleName(),
                result.getResolvedException().getClass().getSimpleName()
        );

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원 문의 분류 삭제. 분류 아이디가 1보다 작은 경우")
    void deleteQnAClassificationValidationIdLT1() throws Exception {
        MvcResult result = mockMvc.perform(delete(URL_PREFIX + "qna/classification/0")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(
                HandlerMethodValidationException.class.getSimpleName(),
                result.getResolvedException().getClass().getSimpleName()
        );

        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertNull(response.errors());
    }
}
