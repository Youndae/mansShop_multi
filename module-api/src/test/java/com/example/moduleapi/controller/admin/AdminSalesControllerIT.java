package com.example.moduleapi.controller.admin;

import com.example.moduleadmin.model.dto.page.AdminSalesPageDTO;
import com.example.moduleadmin.model.dto.sales.business.AdminClassificationSalesProductListDTO;
import com.example.moduleadmin.model.dto.sales.business.AdminProductSalesOptionDTO;
import com.example.moduleadmin.model.dto.sales.out.*;
import com.example.moduleadmin.repository.PeriodSalesSummaryRepository;
import com.example.moduleadmin.repository.ProductSalesSummaryRepository;
import com.example.moduleapi.ModuleApiApplication;
import com.example.moduleapi.config.exception.ExceptionEntity;
import com.example.moduleapi.fixture.TokenFixture;
import com.example.moduleapi.model.response.PagingElementsResponseDTO;
import com.example.moduleapi.model.response.PagingResponseDTO;
import com.example.modulecommon.fixture.*;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.model.enumuration.PageAmount;
import com.example.modulecommon.utils.TestPaginationUtils;
import com.example.moduleconfig.properties.CookieProperties;
import com.example.moduleconfig.properties.TokenProperties;
import com.example.moduleorder.model.dto.admin.out.AdminDailySalesResponseDTO;
import com.example.moduleorder.repository.ProductOrderRepository;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
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
public class AdminSalesControllerIT {

    @Autowired
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
    private ProductOrderRepository productOrderRepository;

    @Autowired
    private PeriodSalesSummaryRepository periodSalesSummaryRepository;

    @Autowired
    private ProductSalesSummaryRepository productSalesSummaryRepository;

    @Autowired
    private TokenProperties tokenProperties;

    @Autowired
    private CookieProperties cookieProperties;

    private Map<String, String> tokenMap;

    private String accessTokenValue;

    private String refreshTokenValue;

    private String inoValue;

    private List<ProductSalesSummary> productSalesSummaryList;

    private List<PeriodSalesSummary> periodSalesSummaryList;

    private List<Classification> classificationList;

    private List<Product> productList;

    private List<ProductOption> productOptionList;

    private List<ProductOrder> orderList;

    private static final int TERM_YEAR = 2024;

    private static final String TERM_MONTH = "2024-01";

    private static final String TERM = "2024-01-01";

    private static final String URL_PREFIX = "/api/admin/";

    @BeforeEach
    void init() {
        MemberAndAuthFixtureDTO memberAndAuthFixtureDTO = MemberAndAuthFixture.createDefaultMember(10);
        MemberAndAuthFixtureDTO adminFixture = MemberAndAuthFixture.createAdmin();
        List<Member> memberList = memberAndAuthFixtureDTO.memberList();
        List<Member> saveMemberList = new ArrayList<>(memberList);
        saveMemberList.addAll(adminFixture.memberList());
        List<Auth> saveAuthList = new ArrayList<>(adminFixture.authList());
        saveAuthList.addAll(adminFixture.authList());
        memberRepository.saveAll(saveMemberList);
        authRepository.saveAll(saveAuthList);

        Member member = memberList.get(0);
        Member admin = adminFixture.memberList().get(0);

        tokenMap = tokenFixture.createAndSaveAllToken(admin);
        accessTokenValue = tokenMap.get(tokenProperties.getAccess().getHeader());
        refreshTokenValue = tokenMap.get(tokenProperties.getRefresh().getHeader());
        inoValue = tokenMap.get(cookieProperties.getIno().getHeader());

        classificationList = ClassificationFixture.createClassifications();
        classificationRepository.saveAll(classificationList);
        productList = ProductFixture.createProductFixtureList(30, classificationList.get(0));
        productRepository.saveAll(productList);
        productOptionList = productList.stream()
                .flatMap(v ->
                        v.getProductOptions().stream()
                )
                .toList();
        productOptionRepository.saveAll(productOptionList);

        orderList = ProductOrderFixture.createDefaultProductOrder(memberList, productOptionList);
        productOrderRepository.saveAll(orderList);

        productSalesSummaryList = SalesSummaryFixture.createProductSalesSummary(productList);
        periodSalesSummaryList = SalesSummaryFixture.createPeriodSalesSummary();
        productSalesSummaryRepository.saveAll(productSalesSummaryList);
        periodSalesSummaryRepository.saveAll(periodSalesSummaryList);

        em.flush();
        em.clear();
    }

    @AfterEach
    void cleanUP() {
        String accessKey = tokenMap.get("accessKey");
        String refreshKey = tokenMap.get("refreshKey");

        redisTemplate.delete(accessKey);
        redisTemplate.delete(refreshKey);
    }

    @Test
    @DisplayName(value = "선택 연도의 월별 매출 조회")
    void getPeriodSales() throws Exception {
        long yearSales = 0L;
        long yearSalesQuantity = 0L;
        long yearOrderSalesQuantity = 0L;
        Map<Integer, AdminPeriodSalesListDTO> fixtureMap = new HashMap<>();

        for(PeriodSalesSummary summary : periodSalesSummaryList) {
            if(summary.getPeriod().getYear() == TERM_YEAR) {
                int month = summary.getPeriod().getMonthValue();
                AdminPeriodSalesListDTO mapDTO = fixtureMap.getOrDefault(month, new AdminPeriodSalesListDTO(month));

                AdminPeriodSalesListDTO putDTO = new AdminPeriodSalesListDTO(
                        month,
                        mapDTO.sales() + summary.getSales(),
                        mapDTO.salesQuantity() + summary.getSalesQuantity(),
                        mapDTO.orderQuantity() + summary.getOrderQuantity()
                );

                fixtureMap.put(month, putDTO);
                yearSales += summary.getSales();
                yearSalesQuantity += summary.getSalesQuantity();
                yearOrderSalesQuantity += summary.getOrderQuantity();
            }
        }

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "sales/period/" + TERM_YEAR)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        AdminPeriodSalesResponseDTO<AdminPeriodSalesListDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertEquals(yearSales, response.sales());
        assertEquals(yearSalesQuantity, response.salesQuantity());
        assertEquals(yearOrderSalesQuantity, response.orderQuantity());

        for(AdminPeriodSalesListDTO responseContentDTO : response.content()) {
            AdminPeriodSalesListDTO mapData = fixtureMap.get(responseContentDTO.date());

            assertEquals(mapData.sales(), responseContentDTO.sales());
            assertEquals(mapData.salesQuantity(), responseContentDTO.salesQuantity());
            assertEquals(mapData.orderQuantity(), responseContentDTO.orderQuantity());
        }
    }

    @Test
    @DisplayName(value = "선택 연도의 월별 매출 조회. 데이터가 없는 경우")
    void getPeriodSalesEmpty() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "sales/period/2000")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        AdminPeriodSalesResponseDTO<AdminPeriodSalesListDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());

        for(AdminPeriodSalesListDTO responseContentDTO : response.content()) {
            assertEquals(0, responseContentDTO.sales());
            assertEquals(0, responseContentDTO.salesQuantity());
            assertEquals(0, responseContentDTO.orderQuantity());
        }
    }

    @Test
    @DisplayName(value = "월 매출 조회")
    void getPeriodSalesDetail() throws Exception {
        String[] termSplit = TERM_MONTH.split("-");
        int year = Integer.parseInt(termSplit[0]);
        int month = Integer.parseInt(termSplit[1]);
        LocalDate term = LocalDate.of(year, month, 1);
        int lastDay = YearMonth.from(term).lengthOfMonth();

        long monthSales = 0L;
        long monthSalesQuantity = 0L;
        long monthOrderQuantity = 0L;
        long lastYearMonthSales = 0L;
        long lastYearMonthSalesQuantity = 0L;
        long lastYearMonthOrderQuantity = 0L;
        Map<Integer, AdminPeriodSalesListDTO> dailyMap = new HashMap<>();
        for(PeriodSalesSummary summary : periodSalesSummaryList) {
            if(summary.getPeriod().getYear() == year && summary.getPeriod().getMonthValue() == month) {
                monthSales += summary.getSales();
                monthSalesQuantity += summary.getSalesQuantity();
                monthOrderQuantity += summary.getOrderQuantity();

                int day = summary.getPeriod().getDayOfMonth();
                AdminPeriodSalesListDTO mapDTO = dailyMap.getOrDefault(day, new AdminPeriodSalesListDTO(day));

                AdminPeriodSalesListDTO putDTO = new AdminPeriodSalesListDTO(
                        day,
                        mapDTO.sales() + summary.getSales(),
                        mapDTO.salesQuantity() + summary.getSalesQuantity(),
                        mapDTO.orderQuantity() + summary.getOrderQuantity()
                );

                dailyMap.put(day, putDTO);
            }
        }

        Map<String, AdminPeriodClassificationDTO> classificationMap = new HashMap<>();
        for(ProductSalesSummary summary : productSalesSummaryList) {
            if(summary.getPeriodMonth().getYear() == year && summary.getPeriodMonth().getMonthValue() == month) {
                String classificationId = summary.getClassification().getId();
                AdminPeriodClassificationDTO mapDTO = classificationMap.getOrDefault(classificationId, new AdminPeriodClassificationDTO(classificationId));

                AdminPeriodClassificationDTO putDTO = new AdminPeriodClassificationDTO(
                        classificationId,
                        mapDTO.classificationSales() + summary.getSales(),
                        mapDTO.classificationSalesQuantity() + summary.getSalesQuantity()
                );

                classificationMap.put(classificationId, putDTO);
            }
        }

        for(PeriodSalesSummary summary : periodSalesSummaryList) {
            if(summary.getPeriod().getYear() == year - 1 && summary.getPeriod().getMonthValue() == month) {
                lastYearMonthSales += summary.getSales();
                lastYearMonthSalesQuantity += summary.getSalesQuantity();
                lastYearMonthOrderQuantity += summary.getOrderQuantity();
            }
        }

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "sales/period/detail/" + TERM_MONTH)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        AdminPeriodMonthDetailResponseDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(monthSales, response.monthSales());
        assertEquals(monthSalesQuantity, response.monthSalesQuantity());
        assertEquals(monthOrderQuantity, response.monthOrderQuantity());
        assertEquals(monthSales - lastYearMonthSales, response.lastYearComparison());
        assertEquals(lastYearMonthSales, response.lastYearSales());
        assertEquals(lastYearMonthSalesQuantity, response.lastYearSalesQuantity());
        assertEquals(lastYearMonthOrderQuantity, response.lastYearOrderQuantity());
        assertFalse(response.bestProduct().isEmpty());
        assertEquals(5, response.bestProduct().size());
        assertFalse(response.classificationSales().isEmpty());
        assertEquals(classificationList.size(), response.classificationSales().size());
        assertFalse(response.dailySales().isEmpty());
        assertEquals(lastDay, response.dailySales().size());

        for(int i = 1; i <= lastDay; i++) {
            AdminPeriodSalesListDTO daily = dailyMap.getOrDefault(i, new AdminPeriodSalesListDTO(i));
            AdminPeriodSalesListDTO resultDaily = response.dailySales().get(i - 1);

            assertEquals(daily, resultDaily);
        }

        for(int i = 0; i < response.classificationSales().size(); i++) {
            AdminPeriodClassificationDTO resultDTO = response.classificationSales().get(i);
            AdminPeriodClassificationDTO classification = classificationMap.getOrDefault(resultDTO.classification(), new AdminPeriodClassificationDTO(resultDTO.classification()));

            assertEquals(classification.classificationSales(), resultDTO.classificationSales());
            assertEquals(classification.classificationSalesQuantity(), resultDTO.classificationSalesQuantity());
        }
    }

    @Test
    @DisplayName(value = "월 매출 조회. 데이터가 없는 경우")
    void getPeriodSalesDetailEmpty() throws Exception {
        String emptyTerm = "1900-01";
        String[] termSplit = emptyTerm.split("-");
        int year = Integer.parseInt(termSplit[0]);
        int month = Integer.parseInt(termSplit[1]);
        LocalDate term = LocalDate.of(year, month, 1);
        int lastDay = YearMonth.from(term).lengthOfMonth();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "sales/period/detail/" + emptyTerm)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        AdminPeriodMonthDetailResponseDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(0, response.monthSales());
        assertEquals(0, response.monthSalesQuantity());
        assertEquals(0, response.monthOrderQuantity());
        assertEquals(0, response.lastYearComparison());
        assertEquals(0, response.lastYearSales());
        assertEquals(0, response.lastYearSalesQuantity());
        assertEquals(0, response.lastYearOrderQuantity());
        assertTrue(response.bestProduct().isEmpty());
        assertFalse(response.classificationSales().isEmpty());
        assertEquals(classificationList.size(), response.classificationSales().size());
        assertFalse(response.dailySales().isEmpty());
        assertEquals(lastDay, response.dailySales().size());

        for(AdminPeriodClassificationDTO classificationDTO : response.classificationSales()) {
            assertEquals(0, classificationDTO.classificationSales());
            assertEquals(0, classificationDTO.classificationSalesQuantity());
        }

        for(AdminPeriodSalesListDTO daily : response.dailySales()) {
            assertEquals(0, daily.sales());
            assertEquals(0, daily.salesQuantity());
            assertEquals(0, daily.orderQuantity());
        }
    }

    @Test
    @DisplayName(value = "선택한 상품 분류의 월 매출 조회")
    void getSalesByClassification() throws Exception {
        String[] termSplit = TERM_MONTH.split("-");
        int year = Integer.parseInt(termSplit[0]);
        int month = Integer.parseInt(termSplit[1]);
        String classification = classificationList.get(0).getId();

        long totalSales = 0L;
        long totalSalesQuantity = 0L;
        List<AdminClassificationSalesProductListDTO> productListDTO = new ArrayList<>();
        for(ProductSalesSummary summary : productSalesSummaryList) {
            if(summary.getPeriodMonth().getYear() == year
                    && summary.getPeriodMonth().getMonthValue() == month
                    && summary.getClassification().getId().equals(classification)) {
                totalSales += summary.getSales();
                totalSalesQuantity += summary.getSalesQuantity();

                productListDTO.add(
                        new AdminClassificationSalesProductListDTO(
                                summary.getProduct().getProductName(),
                                summary.getProductOption().getSize(),
                                summary.getProductOption().getColor(),
                                summary.getSales(),
                                summary.getSalesQuantity()
                        )
                );
            }
        }

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "sales/period/detail/classification")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("term", TERM_MONTH)
                        .param("classification", classification))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        AdminClassificationSalesResponseDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(classification, response.classification());
        assertEquals(totalSales, response.totalSales());
        assertEquals(totalSalesQuantity, response.totalSalesQuantity());
        assertFalse(response.productList().isEmpty());
        assertEquals(productListDTO.size(), response.productList().size());
    }

    @Test
    @DisplayName(value = "선택한 상품 분류의 월 매출 조회. 데이터가 없는 경우")
    void getSalesByClassificationEmpty() throws Exception {
        productSalesSummaryRepository.deleteAll();
        String classification = classificationList.get(0).getId();
        int productSize = productOptionList.stream()
                .filter(v -> v.getProduct().getClassification().getId().equals(classification))
                .toList()
                .size();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "sales/period/detail/classification")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("term", TERM_MONTH)
                        .param("classification", classification))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        AdminClassificationSalesResponseDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(classification, response.classification());
        assertEquals(0, response.totalSales());
        assertEquals(0, response.totalSalesQuantity());
        assertFalse(response.productList().isEmpty());
        assertEquals(productSize, response.productList().size());

        for(AdminClassificationSalesProductListDTO resultDTO : response.productList()) {
            assertEquals(0, resultDTO.productSales());
            assertEquals(0, resultDTO.productSalesQuantity());
        }
    }

    @Test
    @DisplayName(value = "일 매출 조회")
    void getSalesByDay() throws Exception {
        int[] termSplit = Arrays.stream(TERM.split("-"))
                .mapToInt(Integer::parseInt)
                .toArray();
        int year = termSplit[0];
        int month = termSplit[1];
        int day = termSplit[2];
        long totalSales = 0L;
        long totalSalesQuantity = 0L;
        long totalOrderQuantity = 0L;
        Map<String, AdminPeriodClassificationDTO> classificationMap = new HashMap<>();

        for(ProductSalesSummary summary : productSalesSummaryList) {
            if(summary.getPeriodMonth().getYear() == year
                    && summary.getPeriodMonth().getMonthValue() == month
                    && summary.getPeriodMonth().getDayOfMonth() == day) {
                String classification = summary.getClassification().getId();
                AdminPeriodClassificationDTO mapData = classificationMap.getOrDefault(classification, new AdminPeriodClassificationDTO(classification));

                classificationMap.put(
                        classification,
                        new AdminPeriodClassificationDTO(
                                classification,
                                mapData.classificationSales() + summary.getSales(),
                                mapData.classificationSalesQuantity() + summary.getSalesQuantity()
                        )
                );
            }
        }

        for(PeriodSalesSummary summary : periodSalesSummaryList) {
            if(summary.getPeriod().getYear() == year
                    && summary.getPeriod().getMonthValue() == month
                    && summary.getPeriod().getDayOfMonth() == day) {
                totalSales = summary.getSales();
                totalSalesQuantity = summary.getSalesQuantity();
                totalOrderQuantity = summary.getOrderQuantity();

                break;
            }
        }

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "sales/period/detail/day")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("term", TERM))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        AdminPeriodSalesResponseDTO<AdminPeriodClassificationDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertEquals(classificationList.size(), response.content().size());
        assertEquals(totalSales, response.sales());
        assertEquals(totalSalesQuantity, response.salesQuantity());
        assertEquals(totalOrderQuantity, response.orderQuantity());

        for(AdminPeriodClassificationDTO classificationDTO : response.content()) {
            String resultClassification = classificationDTO.classification();
            AdminPeriodClassificationDTO fixture = classificationMap.getOrDefault(resultClassification, new AdminPeriodClassificationDTO(resultClassification));

            assertEquals(fixture, classificationDTO);
        }
    }

    @Test
    @DisplayName(value = "일 매출 조회. 데이터가 없는 경우")
    void getSalesByDayEmpty() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "sales/period/detail/day")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("term", "1900-01-01"))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        AdminPeriodSalesResponseDTO<AdminPeriodClassificationDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertEquals(0, response.sales());
        assertEquals(0, response.salesQuantity());
        assertEquals(0, response.orderQuantity());
    }

    @Test
    @DisplayName(value = "선택 일자의 모든 주문 목록 조회")
    void getOrderListByDay() throws Exception {
        LocalDate orderTerm = LocalDate.now().minusDays(1);
        String termParam = orderTerm.toString();
        int year = orderTerm.getYear();
        int month = orderTerm.getMonthValue();
        int day = orderTerm.getDayOfMonth();
        int amount = PageAmount.ADMIN_DAILY_ORDER_AMOUNT.getAmount();
        List<ProductOrder> orderFixtureList = orderList.stream()
                .filter(v -> v.getCreatedAt().getYear() == year
                        && v.getCreatedAt().getMonthValue() == month
                        && v.getCreatedAt().getDayOfMonth() == day)
                .toList();
        int totalPages = TestPaginationUtils.getTotalPages(orderFixtureList.size(), amount);
        int contentSize = Math.min(orderFixtureList.size(), amount);

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "sales/period/order-list")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("term", termParam)
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingElementsResponseDTO<AdminDailySalesResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertEquals(contentSize, response.content().size());
        assertEquals(orderFixtureList.size(), response.totalElements());
        assertEquals(totalPages, response.totalPages());
        assertFalse(response.empty());

        response.content().forEach(v -> assertFalse(v.detailList().isEmpty()));
    }

    @Test
    @DisplayName(value = "선택 일자의 모든 주문 목록 조회. 데이터가 없는 경우")
    void getOrderListByDayEmpty() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "sales/period/order-list")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("term", "1900-01-01")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingElementsResponseDTO<AdminDailySalesResponseDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertEquals(0, response.totalElements());
        assertEquals(0, response.totalPages());
        assertTrue(response.empty());
    }

    @Test
    @DisplayName(value = "상품별 매출 조회")
    void getProductSales() throws Exception {
        AdminSalesPageDTO pageDTO = new AdminSalesPageDTO(null, 1);
        int totalPages = TestPaginationUtils.getTotalPages(productList.size(), pageDTO.amount());
        int contentSize = Math.min(productList.size(), pageDTO.amount());

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "sales/product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminProductSalesListDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(totalPages, response.totalPages());
        assertEquals(contentSize, response.content().size());
    }

    @Test
    @DisplayName(value = "상품별 매출 조회. 상품명 기반 검색")
    void getProductSalesSearch() throws Exception {
        AdminSalesPageDTO pageDTO = new AdminSalesPageDTO(null, 1);
        Product productFixture = productList.get(0);
        Map<String, AdminProductSalesListDTO> fixtureMap = new HashMap<>();

        for(ProductSalesSummary summary : productSalesSummaryList) {
            if(summary.getProduct().getProductName().contains(productFixture.getProductName())) {

                Product summaryProduct = summary.getProduct();
                AdminProductSalesListDTO mapData = fixtureMap.getOrDefault(summaryProduct.getId(), null);
                if(mapData == null) {
                    fixtureMap.put(summaryProduct.getId(), new AdminProductSalesListDTO(
                            summaryProduct.getClassification().getId(),
                            summaryProduct.getId(),
                            summaryProduct.getProductName(),
                            summary.getSales(),
                            summaryProduct.getProductSalesQuantity()
                    ));
                }else {
                    long sales = mapData.sales() + summary.getSales();
                    fixtureMap.put(summaryProduct.getId(), new AdminProductSalesListDTO(
                            summaryProduct.getClassification().getId(),
                            summaryProduct.getId(),
                            summaryProduct.getProductName(),
                            sales,
                            summaryProduct.getProductSalesQuantity()
                    ));
                }
            }
        }
        int contentSize = Math.min(fixtureMap.size(), pageDTO.amount());
        int totalPages = TestPaginationUtils.getTotalPages(fixtureMap.size(), pageDTO.amount());

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "sales/product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .param("keyword", productFixture.getProductName()))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminProductSalesListDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(totalPages, response.totalPages());
        assertEquals(contentSize, response.content().size());

        for(AdminProductSalesListDTO responseDTO : response.content()) {
            AdminProductSalesListDTO mapData = fixtureMap.getOrDefault(responseDTO.productId(), null);

            assertNotNull(mapData);
            assertEquals(mapData.productId(), responseDTO.productId());
            assertEquals(mapData.productName(), responseDTO.productName());
            assertEquals(mapData.sales(), responseDTO.sales());
            assertEquals(mapData.salesQuantity(), responseDTO.salesQuantity());
        }
    }

    @Test
    @DisplayName(value = "상품별 매출 조회. 상품이 존재하지만 매출 데이터가 아직 없는 경우")
    void getProductSalesSummaryIsEmpty() throws Exception {
        productSalesSummaryRepository.deleteAll();
        AdminSalesPageDTO pageDTO = new AdminSalesPageDTO(null, 1);
        int totalPages = TestPaginationUtils.getTotalPages(productList.size(), pageDTO.amount());
        int contentSize = Math.min(productList.size(), pageDTO.amount());

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "sales/product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminProductSalesListDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertFalse(response.content().isEmpty());
        assertFalse(response.empty());
        assertEquals(totalPages, response.totalPages());
        assertEquals(contentSize, response.content().size());

        response.content().forEach(v -> assertEquals(0, v.sales()));
    }

    @Test
    @DisplayName(value = "상품별 매출 조회. 상품, 주문, 매출 데이터가 모두 없는 경우")
    void getProductSalesSummaryAllDataEmpty() throws Exception {
        productOrderRepository.deleteAll();
        productSalesSummaryRepository.deleteAll();
        productOptionRepository.deleteAll();
        productRepository.deleteAll();

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "sales/product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        PagingResponseDTO<AdminProductSalesListDTO> response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertTrue(response.empty());
        assertEquals(0, response.totalPages());
    }

    @Test
    @DisplayName(value = "선택 상품의 매출 상세 내역 조회")
    void getProductSalesDetail() throws Exception {
        Product fixture = productList.get(0);
        int year = LocalDate.now().getYear();
        long totalSales = 0L;
        long totalSalesQuantity = fixture.getProductSalesQuantity();
        long sales = 0L;
        long salesQuantity = 0L;
        long lastYearSales = 0L;
        long lastYearSalesQuantity = 0L;
        Map<Long, AdminProductSalesOptionDTO> optionTotalMap = new HashMap<>();
        Map<Integer, AdminPeriodSalesListDTO> monthSalesMap = new HashMap<>();
        Map<Long, AdminProductSalesOptionDTO> optionThisYearMap = new HashMap<>();
        Map<Long, AdminProductSalesOptionDTO> optionLastYearMap = new HashMap<>();
        for(ProductSalesSummary summary : productSalesSummaryList) {
            if(summary.getProduct().getId().equals(fixture.getId())) {
                totalSales += summary.getSales();

                AdminProductSalesOptionDTO totalMap = optionTotalMap.getOrDefault(summary.getProductOption().getId(), new AdminProductSalesOptionDTO(summary.getProductOption()));
                optionTotalMap.put(
                        summary.getProductOption().getId(),
                        new AdminProductSalesOptionDTO(
                                summary.getProductOption(),
                                totalMap.optionSales() + summary.getSales(),
                                totalMap.optionSalesQuantity() + summary.getSalesQuantity()
                        )
                );


                if(summary.getPeriodMonth().getYear() == year) {
                    sales += summary.getSales();
                    salesQuantity += summary.getSalesQuantity();
                    int month = summary.getPeriodMonth().getMonthValue();
                    AdminPeriodSalesListDTO mapData = monthSalesMap.getOrDefault(month, new AdminPeriodSalesListDTO(month));

                    monthSalesMap.put(
                            summary.getPeriodMonth().getMonthValue(),
                            new AdminPeriodSalesListDTO(
                                    month,
                                    mapData.sales() + summary.getSales(),
                                    mapData.salesQuantity() + summary.getSalesQuantity(),
                                    mapData.orderQuantity() + summary.getOrderQuantity()
                            )
                    );

                    ProductOption option = summary.getProductOption();
                    AdminProductSalesOptionDTO yearMap = optionThisYearMap.getOrDefault(option.getId(), new AdminProductSalesOptionDTO(option));

                    optionThisYearMap.put(
                            option.getId(),
                            new AdminProductSalesOptionDTO(
                                    option,
                                    yearMap.optionSales() + summary.getSales(),
                                    yearMap.optionSalesQuantity() + summary.getSalesQuantity()
                            )
                    );
                }

                if(summary.getPeriodMonth().getYear() == (year - 1)){
                    lastYearSales += summary.getSales();
                    lastYearSalesQuantity += summary.getSalesQuantity();

                    ProductOption option = summary.getProductOption();
                    AdminProductSalesOptionDTO yearMap = optionLastYearMap.getOrDefault(option.getId(), new AdminProductSalesOptionDTO(option));

                    optionLastYearMap.put(
                            option.getId(),
                            new AdminProductSalesOptionDTO(
                                    option,
                                    yearMap.optionSales() + summary.getSales(),
                                    yearMap.optionSalesQuantity() + summary.getSalesQuantity()
                            )
                    );
                }
            }
        }

        MvcResult result = mockMvc.perform(get(URL_PREFIX + "sales/product/detail/" + fixture.getId())
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        AdminProductSalesDetailDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(fixture.getProductName(), response.productName());
        assertEquals(totalSales, response.totalSales());
        assertEquals(totalSalesQuantity, response.totalSalesQuantity());
        assertEquals(sales, response.yearSales());
        assertEquals(salesQuantity, response.yearSalesQuantity());
        assertEquals((sales - lastYearSales), response.lastYearComparison());
        assertEquals(lastYearSales, response.lastYearSales());
        assertEquals(lastYearSalesQuantity, response.lastYearSalesQuantity());
        assertFalse(response.monthSales().isEmpty());
        assertEquals(12, response.monthSales().size());

        for(AdminPeriodSalesListDTO monthSales : response.monthSales()) {
            int month = monthSales.date();
            AdminPeriodSalesListDTO mapData = monthSalesMap.getOrDefault(
                    month,
                    new AdminPeriodSalesListDTO(month)
            );

            assertEquals(mapData.sales(), monthSales.sales());
            assertEquals(mapData.salesQuantity(), monthSales.salesQuantity());
            assertEquals(mapData.orderQuantity(), monthSales.orderQuantity());
        }

        assertFalse(response.optionTotalSales().isEmpty());
        assertEquals(optionTotalMap.size(), response.optionTotalSales().size());

        for(AdminProductSalesOptionDTO optionSales : response.optionTotalSales()) {
            long optionId = optionSales.optionId();

            AdminProductSalesOptionDTO mapData = optionTotalMap.getOrDefault(
                    optionId,
                    new AdminProductSalesOptionDTO(optionId, optionSales.size(), optionSales.color())
            );

            assertEquals(mapData.optionSales(), optionSales.optionSales());
            assertEquals(mapData.optionSalesQuantity(), optionSales.optionSalesQuantity());
        }

        assertFalse(response.optionYearSales().isEmpty());
        assertEquals(optionThisYearMap.size(), response.optionYearSales().size());

        for(int i = 0; i < response.optionYearSales().size(); i++) {
            AdminProductSalesOptionDTO resultList = response.optionYearSales().get(i);
            AdminProductSalesOptionDTO fixtureDTO = optionThisYearMap.getOrDefault(resultList.optionId(), new AdminProductSalesOptionDTO(resultList.optionId(), resultList.size(), resultList.color()));

            assertEquals(fixtureDTO.optionId(), resultList.optionId());
            assertEquals(fixtureDTO.size(), resultList.size());
            assertEquals(fixtureDTO.color(), resultList.color());
            assertEquals(fixtureDTO.optionSales(), resultList.optionSales());
            assertEquals(fixtureDTO.optionSalesQuantity(), resultList.optionSalesQuantity());
        }

        assertFalse(response.optionLastYearSales().isEmpty());
        assertEquals(optionLastYearMap.size(), response.optionLastYearSales().size());

        for(int i = 0; i < response.optionLastYearSales().size(); i++) {
            AdminProductSalesOptionDTO resultList = response.optionLastYearSales().get(i);
            AdminProductSalesOptionDTO fixtureDTO = optionLastYearMap.getOrDefault(resultList.optionId(), new AdminProductSalesOptionDTO(resultList.optionId(), resultList.size(), resultList.color()));

            assertEquals(fixtureDTO.optionId(), resultList.optionId());
            assertEquals(fixtureDTO.size(), resultList.size());
            assertEquals(fixtureDTO.color(), resultList.color());
            assertEquals(fixtureDTO.optionSales(), resultList.optionSales());
            assertEquals(fixtureDTO.optionSalesQuantity(), resultList.optionSalesQuantity());
        }
    }

    @Test
    @DisplayName(value = "선택 상품의 매출 상세 내역 조회. 상품 아이디가 잘못된 경우")
    void getProductSalesDetailWrongProductId() throws Exception {
        MvcResult result = mockMvc.perform(get(URL_PREFIX + "sales/product/detail/noneProductId")
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
}
