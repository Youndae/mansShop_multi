package com.example.moduleapi.controller.product;

import com.example.moduleapi.ModuleApiApplication;
import com.example.moduleapi.config.exception.ExceptionEntity;
import com.example.moduleapi.config.exception.ValidationError;
import com.example.moduleapi.config.exception.ValidationExceptionEntity;
import com.example.moduleapi.model.response.PagingResponseDTO;
import com.example.modulecommon.fixture.ClassificationFixture;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.fixture.ProductOrderFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.utils.TestPaginationUtils;
import com.example.moduleorder.repository.ProductOrderRepository;
import com.example.moduleproduct.model.dto.main.out.MainListResponseDTO;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import com.example.moduleuser.repository.AuthRepository;
import com.example.moduleuser.repository.MemberRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class MainControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

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

    private List<Classification> classificationList;

    private List<Product> productList;

    private List<ProductOption> productOptionList;

    private List<ProductOrder> anonymousProductOrderList;

    private static final String URL_PREFIX = "/api/main/";


    @BeforeEach
    void init() {
        MemberAndAuthFixtureDTO anonymousFixtureDTO = MemberAndAuthFixture.createAnonymous();
        Member anonymous = anonymousFixtureDTO.memberList().get(0);
        memberRepository.save(anonymous);
        authRepository.saveAll(anonymousFixtureDTO.authList());

        classificationList = ClassificationFixture.createClassifications();
        classificationRepository.saveAll(classificationList);

        productList = ProductFixture.createProductFixtureList(50, classificationList.get(0));
        productOptionList = productList.stream()
                .flatMap(v -> v.getProductOptions().stream())
                .toList();
        productRepository.saveAll(productList);
        productOptionRepository.saveAll(productOptionList);

        anonymousProductOrderList = ProductOrderFixture.createDefaultProductOrder(List.of(anonymous), productOptionList);
        productOrderRepository.saveAll(anonymousProductOrderList);
    }

    @Test
    @DisplayName(value = "메인 BEST 상품 조회")
    void getBestProductList() throws Exception{
        List<Product> productFixture = productList.stream()
                .sorted(Comparator.comparingLong(Product::getProductSalesQuantity).reversed())
                .limit(12)
                .toList();
        MvcResult result = mockMvc.perform(get(URL_PREFIX))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        List<MainListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertFalse(response.isEmpty());
        assertEquals(productFixture.size(), response.size());

        for(int i = 0; i < productFixture.size(); i++) {
            Product product = productFixture.get(i);
            MainListResponseDTO responseDTO = response.get(i);

            int discountPrice = (int) (product.getProductPrice() * (1 - ((double) product.getProductDiscount() / 100)));
            int stock = product.getProductOptions().stream().mapToInt(ProductOption::getStock).sum();

            assertEquals(product.getId(), responseDTO.productId());
            assertEquals(product.getProductName(), responseDTO.productName());
            assertEquals(product.getThumbnail(), responseDTO.thumbnail());
            assertEquals(product.getProductPrice(), responseDTO.originPrice());
            assertEquals(product.getProductDiscount(), responseDTO.discount());
            assertEquals(discountPrice, responseDTO.discountPrice());
            assertEquals(stock == 0, responseDTO.isSoldOut());
        }
    }

    @Test
    @DisplayName(value = "메인 BEST 상품 조회. 데이터가 없는 경우")
    void getBestProductListEmpty() throws Exception {
        productRepository.deleteAll();
        MvcResult result = mockMvc.perform(get(URL_PREFIX))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        List<MainListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertTrue(response.isEmpty());
    }

    @Test
    @DisplayName(value = "메인 NEW 상품 조회")
    void getNewProductList() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "new"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        List<MainListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertFalse(response.isEmpty());
        assertEquals(12, response.size());
    }

    @Test
    @DisplayName(value = "메인 NEW 상품 조회. 데이터가 없는 경우")
    void getNewProductListEmpty() throws Exception {
        productRepository.deleteAll();
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "new"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        List<MainListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertTrue(response.isEmpty());
    }

    @Test
    @DisplayName(value = "메인 OUTER 상품 조회")
    void getOUTERProductList() throws Exception {
        String classificationId = classificationList.get(0).getId();
        List<Product> fixtureList = productList.stream()
                .filter(v -> v.getClassification().getId().equals(classificationId))
                .toList();
        int contentSize = Math.min(fixtureList.size(), 12);
        int totalPages = TestPaginationUtils.getTotalPages(fixtureList.size(), 12);
        MvcResult result = mockMvc.perform(get(URL_PREFIX + classificationId))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        PagingResponseDTO<MainListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(contentSize, response.content().size());
        assertEquals(totalPages, response.totalPages());
    }

    @Test
    @DisplayName(value = "메인 OUTER 상품 조회. 데이터가 없는 경우")
    void getOUTERProductListEmpty() throws Exception {
        productRepository.deleteAll();
        String classificationId = classificationList.get(0).getId();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + classificationId))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        PagingResponseDTO<MainListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertTrue(response.content().isEmpty());
        assertTrue(response.empty());
        assertEquals(0, response.totalPages());
    }

    @Test
    @DisplayName(value = "메인 상품 분류별 조회. 잘못된 상품 분류값을 넘기는 경우 오류 발생없이 빈 리스트 반환")
    void getOUTERProductListWrongClassification() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "wrongClassification"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        PagingResponseDTO<MainListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertTrue(response.content().isEmpty());
        assertTrue(response.empty());
        assertEquals(0, response.number());
        assertEquals(0, response.totalPages());
    }

    @Test
    @DisplayName(value = "메인 OUTER 상품 조회. page 값이 1보다 작은 경우")
    void getOUTERProductListValidationPageIsZero() throws Exception {
        String classificationId = classificationList.get(0).getId();
        MvcResult result = mockMvc.perform(get(URL_PREFIX + classificationId)
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
    @DisplayName(value = "상품 검색")
    void searchList() throws Exception{
        Product fixture = productList.get(0);

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "search")
                        .param("keyword", fixture.getProductName()))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        PagingResponseDTO<MainListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(1, response.content().size());
        assertEquals(1, response.totalPages());

        MainListResponseDTO responseDTO = response.content().get(0);
        assertEquals(fixture.getId(), responseDTO.productId());
        assertEquals(fixture.getProductName(), responseDTO.productName());
        assertEquals(fixture.getThumbnail(), responseDTO.thumbnail());
        assertEquals(fixture.getProductPrice(), responseDTO.originPrice());
        assertEquals(fixture.getProductDiscount(), responseDTO.discount());
    }

    @Test
    @DisplayName(value = "상품 검색. 데이터가 없는 경우")
    void searchListEmpty() throws Exception{
        String productName = "noneProductName";
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "search")
                        .param("keyword", productName))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        PagingResponseDTO<MainListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertTrue(response.content().isEmpty());
        assertTrue(response.empty());
        assertEquals(0, response.totalPages());
    }

    @Test
    @DisplayName(value = "상품 검색. 키워드가 없는 경우")
    void searchListValidationKeywordIsNull() throws Exception{
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "search"))
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

        assertEquals("keyword", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 검색. 키워드가 Blank인 경우")
    void searchListValidationKeywordIsBlank() throws Exception{
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "search")
                        .param("keyword", ""))
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

        List<String> validationConstraintList = List.of("NotBlank", "Size");

        response.errors().forEach(v -> {
            assertEquals("keyword", v.field());
            assertTrue(validationConstraintList.contains(v.constraint()));
        });
    }

    @Test
    @DisplayName(value = "상품 검색. 키워드가 한글자인 경우")
    void searchListValidationKeywordLength1() throws Exception{
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "search")
                        .param("keyword", "a"))
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

        assertEquals("keyword", responseObj.field());
        assertEquals("Size", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 검색. 페이지값이 1보다 작은 경우")
    void searchListValidationPageIsZero() throws Exception{
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "search")
                        .param("keyword", "search")
                        .param("page", "0"))
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

        assertEquals("page", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "상품 검색. 키워드가 Blank, page가 0인 경우")
    void searchListValidationKeywordIsBlankAndPageIsZero() throws Exception{
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "search")
                        .param("keyword", "")
                        .param("page", "0"))
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

        assertEquals(3, response.errors().size());

        List<String> keywordValidationConstraintList = List.of("NotBlank", "Size");

        response.errors().forEach(v -> {
            if(v.field().equalsIgnoreCase("keyword")) {
                assertTrue(keywordValidationConstraintList.contains(v.constraint()));
            }else if(v.field().equalsIgnoreCase("page")){
                assertEquals("Min", v.constraint());
            }else
                fail();
        });
    }

    @Test
    @DisplayName(value = "비회원의 주문 내역 조회")
    void getNoneMemberOrderList() throws Exception{
        ProductOrder fixture = anonymousProductOrderList.get(0);
        String phone = fixture.getOrderPhone().replaceAll("-", "");
        String term = "3";
        int amount = 20;
        int contentSize = Math.min(anonymousProductOrderList.size(), amount);
        int totalPages = TestPaginationUtils.getTotalPages(anonymousProductOrderList.size(), amount);

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "/order/" + term)
                        .param("recipient", fixture.getRecipient())
                        .param("phone", phone)
                        .param("page", "1")
                )
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        PagingResponseDTO<MainListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(contentSize, response.content().size());
        assertEquals(totalPages, response.totalPages());
    }

    @Test
    @DisplayName(value = "비회원의 주문 내역 조회. 데이터가 없는 경우")
    void getNoneMemberOrderListEmpty() throws Exception{
        String term = "3";

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "/order/" + term)
                        .param("recipient", "noneRecipient")
                        .param("phone", "01090908080")
                        .param("page", "1")
                )
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();

        PagingResponseDTO<MainListResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertTrue(response.content().isEmpty());
        assertTrue(response.empty());
        assertEquals(0, response.totalPages());
    }

    @Test
    @DisplayName(value = "비회원의 주문 내역 조회. 수령인이 null인 경우")
    void getAnonymousOrderListValidationRecipientIsNull() throws Exception{
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "/order/3")
                                    .param("phone", "01000001111")
                                    .param("page", "1")
                                )
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

        assertEquals("recipient", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "비회원의 주문 내역 조회. 수령인이 Blank인 경우")
    void getAnonymousOrderListValidationRecipientIsBlank() throws Exception{
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "/order/3")
                        .param("recipient", "")
                        .param("phone", "01000001111")
                        .param("page", "1")
                )
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

        assertEquals("recipient", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "비회원의 주문 내역 조회. 연락처가 null인 경우")
    void getAnonymousOrderListValidationPhoneIsNull() throws Exception{
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "/order/3")
                        .param("recipient", "tester")
                        .param("page", "1")
                )
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

        assertEquals("phone", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "비회원의 주문 내역 조회. 연락처가 Blank인 경우")
    void getAnonymousOrderListValidationPhoneIsBlank() throws Exception{
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "/order/3")
                        .param("recipient", "tester")
                        .param("phone", "")
                        .param("page", "1")
                )
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

        List<String> phoneValidationConstraintList = List.of("NotBlank", "Pattern");

        response.errors().forEach(v -> {
            assertEquals("phone", v.field());
            assertTrue(phoneValidationConstraintList.contains(v.constraint()));
        });
    }

    @Test
    @DisplayName(value = "비회원의 주문 내역 조회. 연락처에 하이픈이 포함된 경우")
    void getAnonymousOrderListValidationPhoneIncludeHyphen() throws Exception{
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "/order/3")
                        .param("recipient", "tester")
                        .param("phone", "010-0000-1111")
                        .param("page", "1")
                )
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

        assertEquals("phone", responseObj.field());
        assertEquals("Pattern", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "비회원의 주문 내역 조회. 연락처가 짧은 경우")
    void getAnonymousOrderListValidationPhoneToShort() throws Exception{
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "/order/3")
                        .param("recipient", "tester")
                        .param("phone", "01000111")
                        .param("page", "1")
                )
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

        assertEquals("phone", responseObj.field());
        assertEquals("Pattern", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "비회원의 주문 내역 조회. 페이지값이 null인 경우")
    void getAnonymousOrderListValidationPageIsNull() throws Exception{
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "/order/3")
                        .param("recipient", "tester")
                        .param("phone", "01000001111")
                )
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

        assertEquals("page", responseObj.field());
        assertEquals("typeMismatch", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "비회원의 주문 내역 조회. 페이지값이 0인 경우")
    void getAnonymousOrderListValidationPageIsZero() throws Exception{
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "/order/3")
                        .param("recipient", "tester")
                        .param("phone", "01000001111")
                        .param("page", "0")
                )
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

        assertEquals("page", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "비회원의 주문 내역 조회. Term 제외 모든 파라미터가 비정상인 경우")
    void getAnonymousOrderListValidationAllParameterIsWrong() throws Exception{
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "/order/3")
                        .param("recipient", "")
                        .param("phone", "01000001")
                        .param("page", "0")
                )
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

        assertEquals(3, response.errors().size());

        Map<String, String> validationMap = new HashMap<>();
        validationMap.put("recipient", "NotBlank");
        validationMap.put("phone", "Pattern");
        validationMap.put("page", "Min");

        response.errors().forEach(v -> {
            String constraint = validationMap.getOrDefault(v.field(), null);

            assertNotNull(constraint);
            assertEquals(constraint, v.constraint());
        });
    }

    @Test
    @DisplayName(value = "비회원의 주문 내역 조회. term이 비정상인 경우")
    void getNoneMemberOrderListValidationTermIsWrong() throws Exception{
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "/order/8")
                        .param("recipient", "tester")
                        .param("phone", "01000001111")
                        .param("page", "1")
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        Exception ex = result.getResolvedException();
        assertInstanceOf(IllegalArgumentException.class, ex);

        String content = result.getResponse().getContentAsString();

        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getHttpStatus().value(), response.errorCode());
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
    }
}
