package com.example.moduleapi.controller.product;

import com.example.moduleapi.ModuleApiApplication;
import com.example.moduleapi.model.response.PagingResponseDTO;
import com.example.modulecommon.fixture.ClassificationFixture;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.fixture.ProductOrderFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.utils.PaginationUtils;
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

import java.util.Comparator;
import java.util.List;

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
        int totalPages = PaginationUtils.getTotalPages(fixtureList.size(), 12);
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
    void searchListKeywordIsNull() throws Exception{
        mockMvc.perform(get(URL_PREFIX + "search"))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    @DisplayName(value = "비회원의 주문 내역 조회")
    void getNoneMemberOrderList() throws Exception{
        ProductOrder fixture = anonymousProductOrderList.get(0);
        String phone = fixture.getOrderPhone().replaceAll("-", "");
        String term = "3";
        int amount = 20;
        int contentSize = Math.min(anonymousProductOrderList.size(), amount);
        int totalPages = PaginationUtils.getTotalPages(anonymousProductOrderList.size(), amount);

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "/order/" + term)
                        .param("recipient", fixture.getRecipient())
                        .param("phone", phone))
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
                        .param("phone", "01090908080"))
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
    @DisplayName(value = "요청 파라미터가 없는 경우")
    void getAnonymousOrderListBadRequest() throws Exception{
        String term = "3";

        mockMvc.perform(get(URL_PREFIX + "/order/" + term))
                .andExpect(status().isBadRequest())
                .andReturn();
    }
}
