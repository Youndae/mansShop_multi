package com.example.moduleapi.controller.admin;

import com.example.moduleapi.ModuleApiApplication;
import com.example.moduleapi.config.exception.ExceptionEntity;
import com.example.moduleapi.config.exception.ValidationError;
import com.example.moduleapi.config.exception.ValidationExceptionEntity;
import com.example.moduleapi.fixture.TokenFixture;
import com.example.moduleapi.model.response.PagingResponseDTO;
import com.example.modulecommon.fixture.ClassificationFixture;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.fixture.ProductOrderFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.*;
import com.example.modulecommon.utils.TestPaginationUtils;
import com.example.moduleconfig.properties.CookieProperties;
import com.example.moduleconfig.properties.TokenProperties;
import com.example.modulenotification.repository.NotificationRepository;
import com.example.moduleorder.model.dto.admin.out.AdminOrderResponseDTO;
import com.example.moduleorder.model.dto.admin.page.AdminOrderPageDTO;
import com.example.moduleorder.repository.ProductOrderDetailRepository;
import com.example.moduleorder.repository.ProductOrderRepository;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
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
public class AdminOrderControllerIT {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

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
    private ProductOrderRepository productOrderRepository;

    @Autowired
    private ProductOrderDetailRepository productOrderDetailRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RedisTemplate<String, Long> cacheRedisTemplate;

    @Autowired
    private TokenProperties tokenProperties;

    @Autowired
    private CookieProperties cookieProperties;

    private Map<String, String> tokenMap;

    private String accessTokenValue;

    private String refreshTokenValue;

    private String inoValue;

    private List<ProductOrder> allProductOrderList;

    private List<ProductOrder> newProductOrderList;

    private static final String ORDER_CACHING_KEY = RedisCaching.ADMIN_ORDER_COUNT.getKey();

    private static final String URL_PREFIX = "/api/admin/";

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

        Member admin = adminFixture.memberList().get(0);

        tokenMap = tokenFixture.createAndSaveAllToken(admin);
        accessTokenValue = tokenMap.get(tokenProperties.getAccess().getHeader());
        refreshTokenValue = tokenMap.get(tokenProperties.getRefresh().getHeader());
        inoValue = tokenMap.get(cookieProperties.getIno().getHeader());

        List<Classification> classificationList = ClassificationFixture.createClassifications();
        classificationRepository.saveAll(classificationList);

        List<Product> productList = ProductFixture.createProductFixtureList(5, classificationList.get(0));
        List<ProductOption> productOptionList = productList.stream().flatMap(v -> v.getProductOptions().stream()).toList();
        productRepository.saveAll(productList);
        productOptionRepository.saveAll(productOptionList);

        List<ProductOrder> completeProductOrderList = ProductOrderFixture.createCompleteProductOrder(memberList, productOptionList);
        newProductOrderList = ProductOrderFixture.createDefaultProductOrder(memberList, productOptionList);
        allProductOrderList = new ArrayList<>(completeProductOrderList);
        allProductOrderList.addAll(newProductOrderList);
        productOrderRepository.saveAll(allProductOrderList);
    }

    @AfterEach
    void cleanUP() {
        notificationRepository.deleteAll();
        productOrderDetailRepository.deleteAll();
        productOrderRepository.deleteAll();
        productOptionRepository.deleteAll();
        productRepository.deleteAll();
        classificationRepository.deleteAll();
        authRepository.deleteAll();
        memberRepository.deleteAll();


        String accessKey = tokenMap.get("accessKey");
        String refreshKey = tokenMap.get("refreshKey");

        redisTemplate.delete(accessKey);
        redisTemplate.delete(refreshKey);

        cacheRedisTemplate.delete(ORDER_CACHING_KEY);
    }

    @Test
    @DisplayName(value = "전체 주문 목록 조회")
    void getAllOrder() throws Exception {
        AdminOrderPageDTO pageDTOFixture = new AdminOrderPageDTO(1);
        int contentSize = Math.min(allProductOrderList.size(), pageDTOFixture.amount());
        int totalPages = TestPaginationUtils.getTotalPages(allProductOrderList.size(), pageDTOFixture.amount());

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "order/all")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminOrderResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(contentSize, response.content().size());
        assertEquals(totalPages, response.totalPages());

        Long cachingResult = cacheRedisTemplate.opsForValue().get(ORDER_CACHING_KEY);
        assertNotNull(cachingResult);
        assertEquals(allProductOrderList.size(), cachingResult);
    }

    @Test
    @DisplayName(value = "전체 주문 목록 조회. 받는 사람 기준 검색. 검색은 like가 아닌 equal")
    void getAllOrderSearchRecipient() throws Exception {
        ProductOrder fixture = allProductOrderList.get(0);

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "order/all")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("searchType", "recipient")
                        .param("keyword", fixture.getRecipient()))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminOrderResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(2, response.content().size());
        assertEquals(1, response.totalPages());
        response.content().forEach(v -> assertEquals(fixture.getRecipient(), v.recipient()));

        Long cachingResult = cacheRedisTemplate.opsForValue().get(ORDER_CACHING_KEY);
        assertNull(cachingResult);
    }

    @Test
    @DisplayName(value = "전체 주문 목록 조회. 사용자 아이디 기준 검색. 검색은 like가 아닌 equal")
    void getAllOrderSearchUserId() throws Exception {
        ProductOrder fixture = allProductOrderList.get(0);

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "order/all")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("searchType", "userId")
                        .param("keyword", fixture.getMember().getUserId()))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminOrderResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(2, response.content().size());
        assertEquals(1, response.totalPages());
        response.content().forEach(v -> assertEquals(fixture.getMember().getUserId(), v.userId()));

        Long cachingResult = cacheRedisTemplate.opsForValue().get(ORDER_CACHING_KEY);
        assertNull(cachingResult);
    }

    @Test
    @DisplayName(value = "전체 주문 목록 조회. 데이터가 없는 경우")
    void getAllOrderListEmpty() throws Exception {
        productOrderRepository.deleteAll();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "order/all")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminOrderResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertTrue(response.empty());
        assertEquals(0, response.totalPages());

        Long cachingResult = cacheRedisTemplate.opsForValue().get(ORDER_CACHING_KEY);
        assertEquals(0, cachingResult);
    }

    @Test
    @DisplayName(value = "전체 주문 목록 조회. 페이지값이 0으로 전달된 경우")
    void getAllOrderValidationPageZero() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "order/all")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("page", "0")
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("page", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "전체 주문 목록 조회. 검색어가 한글자인 경우")
    void getAllOrderValidationKeywordLength1() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "order/all")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("keyword", "a")
                        .param("searchType", "recipient")
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("keyword", responseObj.field());
        assertEquals("Size", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "전체 주문 목록 조회. searchType이 유효하지 않은 경우")
    void getAllOrderValidationWrongSearchType() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "order/all")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("keyword", "tester")
                        .param("searchType", "reci")
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "전체 주문 목록 조회. 페이지 값이 0, 검색어가 한글자인 경우")
    void getAllOrderValidationPageZeroAndKeywordLength1() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "order/all")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("page", "0")
                        .param("keyword", "a")
                        .param("searchType", "recipient")
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
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
    @DisplayName(value = "미처리 주문 목록 조회")
    void getNewOrderList() throws Exception {
        AdminOrderPageDTO pageDTOFixture = new AdminOrderPageDTO(1);
        int contentSize = Math.min(newProductOrderList.size(), pageDTOFixture.amount());
        int totalPages = TestPaginationUtils.getTotalPages(newProductOrderList.size(), pageDTOFixture.amount());

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "order/new")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminOrderResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(contentSize, response.content().size());
        assertEquals(totalPages, response.totalPages());

        response.content()
                .forEach(v ->
                        assertEquals(OrderStatus.ORDER.getStatusStr(), v.orderStatus())
                );
    }

    @Test
    @DisplayName(value = "미처리 주문 목록 조회. 받는 사람 기준 검색. 검색은 like가 아닌 equal")
    void getNewOrderSearchRecipient() throws Exception {
        ProductOrder fixture = newProductOrderList.get(0);

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "order/new")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("searchType", "recipient")
                        .param("keyword", fixture.getRecipient()))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminOrderResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(1, response.content().size());
        assertEquals(1, response.totalPages());
        response.content().forEach(v -> {
            assertEquals(fixture.getRecipient(), v.recipient());
            assertEquals(OrderStatus.ORDER.getStatusStr(), v.orderStatus());
        });
    }

    @Test
    @DisplayName(value = "미처리 주문 목록 조회. 사용자 아이디 기준 검색. 검색은 like가 아닌 equal")
    void getNewOrderSearchUserId() throws Exception {
        ProductOrder fixture = newProductOrderList.get(0);

        List<ProductOrder> test = allProductOrderList.stream()
                .filter(v -> v.getMember().getUserId().equals(fixture.getMember().getUserId()))
                .toList();

        test.forEach(v -> System.out.println("order tes : " + v));

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "order/new")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("searchType", "userId")
                        .param("keyword", fixture.getMember().getUserId()))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminOrderResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(1, response.content().size());
        assertEquals(1, response.totalPages());
        response.content().forEach(v -> {
            assertEquals(fixture.getMember().getUserId(), v.userId());
            assertEquals(OrderStatus.ORDER.getStatusStr(), v.orderStatus());
        });
    }

    @Test
    @DisplayName(value = "미처리 주문 목록 조회. 데이터가 없는 경우")
    void getNewOrderListEmpty() throws Exception {
        productOrderRepository.deleteAll();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "order/new")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminOrderResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertTrue(response.empty());
        assertEquals(0, response.totalPages());
    }

    @Test
    @DisplayName(value = "미처리 주문 목록 조회. 페이지값이 0으로 전달된 경우")
    void getNewOrderValidationPageZero() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "order/new")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("page", "0")
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("page", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "미처리 주문 목록 조회. 검색어가 한글자인 경우")
    void getNewOrderValidationKeywordLength1() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "order/new")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("keyword", "a")
                        .param("searchType", "recipient")
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("keyword", responseObj.field());
        assertEquals("Size", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "미처리 주문 목록 조회. searchType이 유효하지 않은 경우")
    void getNewOrderValidationWrongSearchType() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "order/new")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("keyword", "tester")
                        .param("searchType", "reci")
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "미처리 주문 목록 조회. 페이지 값이 0, 검색어가 한글자인 경우")
    void getNewOrderValidationPageZeroAndKeywordLength1() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "order/new")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("page", "0")
                        .param("keyword", "a")
                        .param("searchType", "recipient")
                )
                .andExpect(status().isBadRequest())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
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
    @DisplayName(value = "주문 확인 처리")
    void patchOrderStatus() throws Exception {
        ProductOrder fixture = newProductOrderList.get(0);

        mockMvc.perform(patch(URL_PREFIX + "order/" + fixture.getId())
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isNoContent())
                .andReturn();

        ProductOrder patchData = productOrderRepository.findById(fixture.getId()).orElse(null);
        assertNotNull(patchData);
        assertEquals(OrderStatus.PREPARATION.getStatusStr(), patchData.getOrderStat());

        await()
                .atMost(20, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    List<Notification> notifications = notificationRepository.findAll();
                    assertFalse(notifications.isEmpty());
                    assertEquals(1, notifications.size());

                    Notification notification = notifications.get(0);
                    assertEquals(fixture.getMember().getUserId(), notification.getMember().getUserId());
                    assertEquals(NotificationType.ORDER_STATUS.getType(), notification.getType());
                    assertEquals(NotificationType.ORDER_STATUS.getTitle(), notification.getTitle());
                    assertNull(notification.getRelatedId());
                });
    }

    @Test
    @DisplayName(value = "주문 확인 처리. 주문 아이디가 잘못된 경우")
    void patchOrderStatusWrongId() throws Exception {
        long wrongOrderId = allProductOrderList.get(0).getId() - 100L;

        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "order/" + wrongOrderId)
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
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "주문 확인 처리. 주문 아이디가 0인 경우")
    void patchOrderStatusValidationOrderIdZero() throws Exception {
        MvcResult result = mockMvc.perform(patch(URL_PREFIX + "order/0")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isBadRequest())
                .andReturn();

        Exception ex = result.getResolvedException();
        assertInstanceOf(HandlerMethodValidationException.class, ex);

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
    }
}
