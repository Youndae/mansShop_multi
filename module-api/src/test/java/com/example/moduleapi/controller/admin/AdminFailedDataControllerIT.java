package com.example.moduleapi.controller.admin;

import com.example.moduleadmin.model.dto.failedData.out.FailedQueueDTO;
import com.example.moduleadmin.repository.PeriodSalesSummaryRepository;
import com.example.moduleadmin.repository.ProductSalesSummaryRepository;
import com.example.moduleapi.ModuleApiApplication;
import com.example.moduleapi.config.exception.ExceptionEntity;
import com.example.moduleapi.config.exception.ValidationError;
import com.example.moduleapi.config.exception.ValidationExceptionEntity;
import com.example.moduleapi.fixture.TokenFixture;
import com.example.modulecart.model.dto.business.CartMemberDTO;
import com.example.modulecart.repository.CartRepository;
import com.example.modulecommon.fixture.CartFixture;
import com.example.modulecommon.fixture.ClassificationFixture;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.moduleconfig.config.rabbitMQ.RabbitMQPrefix;
import com.example.moduleconfig.properties.CookieProperties;
import com.example.moduleconfig.properties.RabbitMQProperties;
import com.example.moduleconfig.properties.TokenProperties;
import com.example.moduleorder.model.dto.business.ProductOrderDataDTO;
import com.example.moduleorder.model.dto.in.OrderProductDTO;
import com.example.moduleorder.model.dto.in.PaymentDTO;
import com.example.moduleorder.model.dto.rabbitMQ.OrderCartDTO;
import com.example.moduleorder.model.dto.rabbitMQ.OrderProductMessageDTO;
import com.example.moduleorder.model.dto.rabbitMQ.OrderProductSummaryDTO;
import com.example.moduleorder.model.dto.rabbitMQ.PeriodSummaryQueueDTO;
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
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ModuleApiApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class AdminFailedDataControllerIT {

    @Autowired
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
    private PeriodSalesSummaryRepository periodSalesSummaryRepository;

    @Autowired
    private ProductSalesSummaryRepository productSalesSummaryRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQProperties rabbitMQProperties;

    @Autowired
    private RabbitAdmin rabbitAdmin;

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

    private List<ProductOption> productOptionList;

    private Cart memberCart;

    private List<CartDetail> memberCartDetailList;

    private static final String URL_PREFIX = "/api/admin/";

    @BeforeEach
    void init() {
        MemberAndAuthFixtureDTO memberAndAuthFixtureDTO = MemberAndAuthFixture.createDefaultMember(1);
        MemberAndAuthFixtureDTO adminFixture = MemberAndAuthFixture.createAdmin();
        List<Member> saveMemberList = new ArrayList<>(memberAndAuthFixtureDTO.memberList());
        saveMemberList.addAll(adminFixture.memberList());
        List<Auth> saveAuthList = new ArrayList<>(adminFixture.authList());
        saveAuthList.addAll(adminFixture.authList());
        memberRepository.saveAll(saveMemberList);
        authRepository.saveAll(saveAuthList);

        member = memberAndAuthFixtureDTO.memberList().get(0);
        Member admin = adminFixture.memberList().get(0);

        tokenMap = tokenFixture.createAndSaveAllToken(admin);
        accessTokenValue = tokenMap.get(tokenProperties.getAccess().getHeader());
        refreshTokenValue = tokenMap.get(tokenProperties.getRefresh().getHeader());
        inoValue = tokenMap.get(cookieProperties.getIno().getHeader());

        List<Classification> classificationList = ClassificationFixture.createClassifications();
        classificationRepository.saveAll(classificationList);

        product = ProductFixture.createProductFixtureList(1, classificationList.get(0)).get(0);
        productOptionList = product.getProductOptions();
        productRepository.save(product);
        productOptionRepository.saveAll(productOptionList);

        memberCart = CartFixture.createDefaultMemberCart(List.of(member), productOptionList).get(0);
        cartRepository.save(memberCart);

        memberCartDetailList = memberCart.getCartDetailList();
    }

    @AfterEach
    void cleanUP() {
        String accessKey = tokenMap.get("accessKey");
        String refreshKey = tokenMap.get("refreshKey");

        redisTemplate.delete(accessKey);
        redisTemplate.delete(refreshKey);

        cartRepository.deleteAll();
        productOrderRepository.deleteAll();
        memberRepository.deleteAll();
        productSalesSummaryRepository.deleteAll();
        productOptionRepository.deleteAll();
        productRepository.deleteAll();
        classificationRepository.deleteAll();
        periodSalesSummaryRepository.deleteAll();

        List<String> dlqNames = getDLQNames();
        dlqNames.forEach(v -> rabbitAdmin.purgeQueue(v, false));

        await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    for(String dlq : dlqNames) {
                        Properties queueProperties = rabbitAdmin.getQueueProperties(dlq);
                        Integer messageCount = (Integer) queueProperties.get("QUEUE_MESSAGE_COUNT");
                        assertEquals(0, messageCount);
                    }
                });

        try {
            Thread.sleep(5000);
        }catch (Exception e) {
            e.printStackTrace();
            fail("thread sleep exception");
        }
    }

    private <T> void sendMessage(String exchange, RabbitMQPrefix rabbitMQPrefix, T data) {
        rabbitTemplate.convertAndSend(
                exchange,
                getQueueRoutingKey(rabbitMQPrefix),
                data
        );
    }

    private String getQueueRoutingKey(RabbitMQPrefix rabbitMQPrefix) {
        return rabbitMQProperties.getQueue()
                .get(rabbitMQPrefix.getKey())
                .getDlqRouting();
    }

    private String getOrderExchange() {
        return rabbitMQProperties.getExchange()
                .get(RabbitMQPrefix.EXCHANGE_ORDER.getKey())
                .getDlq();
    }

    private ProductOrderDataDTO createOrderDataDTO(PaymentDTO paymentDTO, CartMemberDTO cartMemberDTO, LocalDateTime createdAt) {
        ProductOrder productOrder = paymentDTO.toOrderEntity(cartMemberDTO.uid(), createdAt);
        List<OrderProductDTO> orderProductList = paymentDTO.orderProduct();
        List<String> orderProductIds = new ArrayList<>();
        List<Long> orderOptionIds = new ArrayList<>();
        int totalProductCount = 0;

        for(OrderProductDTO data : paymentDTO.orderProduct()) {
            productOrder.addDetail(data.toOrderDetailEntity());
            if(!orderProductIds.contains(data.getProductId()))
                orderProductIds.add(data.getProductId());
            orderOptionIds.add(data.getOptionId());
            totalProductCount += data.getDetailCount();
        }
        productOrder.setProductCount(totalProductCount);

        return new ProductOrderDataDTO(productOrder, orderProductList, orderProductIds, orderOptionIds);
    }

    private List<OrderProductDTO> createCartOrderProductDTOFixtureList() {
        List<OrderProductDTO> resultList = new ArrayList<>();
        for(int i = 0; i < memberCartDetailList.size(); i++) {
            ProductOption option = memberCartDetailList.get(i).getProductOption();

            resultList.add(
                    new OrderProductDTO(
                            option.getId(),
                            option.getProduct().getProductName(),
                            option.getProduct().getId(),
                            i + 1,
                            option.getProduct().getProductPrice() * (i + 1)
                    )
            );
        }

        return resultList;
    }

    private List<String> getDLQNames() {
        return rabbitMQProperties.getQueue().values().stream().map(RabbitMQProperties.Queue::getDlq).toList();
    }

    @Test
    @DisplayName(value = "DLQ 메시지 개수 조회")
    void getFailedQueueCount() throws Exception {
        String orderExchange = getOrderExchange();
        List<OrderProductDTO> orderProductList = createCartOrderProductDTOFixtureList();
        int totalPrice = orderProductList.stream().mapToInt(OrderProductDTO::getDetailPrice).sum();
        int deliveryFee = totalPrice < 100000 ? 3500 : 0;
        PaymentDTO paymentDTO = new PaymentDTO(
                "testRecipient",
                "01000010002",
                "testRecipient Memo",
                "testRecipient home",
                orderProductList,
                deliveryFee,
                totalPrice,
                "cash",
                "cart"
        );
        CartMemberDTO cartMemberDTO = new CartMemberDTO(member.getUserId(), null);
        ProductOrderDataDTO productOrderDataDTO = createOrderDataDTO(paymentDTO, cartMemberDTO, LocalDateTime.now());
        ProductOrder order = productOrderDataDTO.productOrder();
        Map<String, RabbitMQProperties.Queue> rabbitMQMap = rabbitMQProperties.getQueue();
        List<String> testQueueNames = List.of(
                rabbitMQMap.get(RabbitMQPrefix.QUEUE_ORDER_CART.getKey()).getDlq(),
                rabbitMQMap.get(RabbitMQPrefix.QUEUE_ORDER_PRODUCT_OPTION.getKey()).getDlq(),
                rabbitMQMap.get(RabbitMQPrefix.QUEUE_ORDER_PRODUCT.getKey()).getDlq(),
                rabbitMQMap.get(RabbitMQPrefix.QUEUE_PERIOD_SUMMARY.getKey()).getDlq(),
                rabbitMQMap.get(RabbitMQPrefix.QUEUE_PRODUCT_SUMMARY.getKey()).getDlq()
        );
        List<FailedQueueDTO> fixtureFailedQueueDTO = rabbitMQProperties.getQueue()
                .values()
                .stream()
                .map(RabbitMQProperties.Queue::getDlq)
                .filter(testQueueNames::contains)
                .map(v -> new FailedQueueDTO(v, 1))
                .toList();

        sendMessage(orderExchange, RabbitMQPrefix.QUEUE_ORDER_CART, new OrderCartDTO(cartMemberDTO, productOrderDataDTO.orderOptionIds()));
        sendMessage(orderExchange, RabbitMQPrefix.QUEUE_ORDER_PRODUCT_OPTION, new OrderProductMessageDTO(productOrderDataDTO));
        sendMessage(orderExchange, RabbitMQPrefix.QUEUE_ORDER_PRODUCT, new OrderProductMessageDTO(productOrderDataDTO));
        sendMessage(orderExchange, RabbitMQPrefix.QUEUE_PERIOD_SUMMARY, new PeriodSummaryQueueDTO(order));
        sendMessage(orderExchange, RabbitMQPrefix.QUEUE_PRODUCT_SUMMARY, new OrderProductSummaryDTO(productOrderDataDTO));

        await()
                .atMost(60, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    MvcResult result = mockMvc.perform(get(URL_PREFIX + "message")
                                    .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                    .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                    .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                            .andExpect(status().isOk())
                            .andReturn();
                    String content = result.getResponse().getContentAsString();
                    List<FailedQueueDTO> response = om.readValue(
                            content,
                            new TypeReference<>() {}
                    );

                    assertNotNull(response);
                    assertFalse(response.isEmpty());
                    assertEquals(fixtureFailedQueueDTO.size(), response.size());
                    fixtureFailedQueueDTO.forEach(v -> assertTrue(response.contains(v)));
                });
    }

    @Test
    @DisplayName(value = "DLQ 메시지 개수 조회. DLQ 메시지가 전혀 없는 경우")
    void getFailedQueueCountEmpty() throws Exception {
        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    MvcResult result = mockMvc.perform(get(URL_PREFIX + "message")
                                    .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                    .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                    .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue)))
                            .andExpect(status().isOk())
                            .andReturn();
                    String content = result.getResponse().getContentAsString();
                    List<FailedQueueDTO> response = om.readValue(
                            content,
                            new TypeReference<>() {}
                    );

                    assertNotNull(response);
                    assertTrue(response.isEmpty());
                });
    }

    @Test
    @DisplayName(value = "DLQ 재처리 요청. queueName이 Null인 경우")
    void postRetryDLQMessagesQueueNameIsNull() throws Exception {
        List<FailedQueueDTO> bodyList = List.of(
                new FailedQueueDTO(null, 1)
        );

        String requestDTO = om.writeValueAsString(bodyList);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "message")
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

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("queueName", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "DLQ 재처리 요청. queueName이 Blank인 경우")
    void postRetryDLQMessagesQueueNameIsBlank() throws Exception {
        List<FailedQueueDTO> bodyList = List.of(
                new FailedQueueDTO("", 1)
        );

        String requestDTO = om.writeValueAsString(bodyList);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "message")
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

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("queueName", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "DLQ 재처리 요청. messageCount가 1보다 작은 경우")
    void postRetryDLQMessagesMessageCountIsZero() throws Exception {
        List<FailedQueueDTO> bodyList = List.of(
                new FailedQueueDTO("testDLQ", 0)
        );

        String requestDTO = om.writeValueAsString(bodyList);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "message")
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

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("messageCount", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "DLQ 재처리 요청. QueueName이 Blank, messageCount가 1보다 작은 경우")
    void postRetryDLQMessagesQueueNameIsBlankAndMessageCountIsZero() throws Exception {
        List<FailedQueueDTO> bodyList = List.of(
                new FailedQueueDTO("", 0)
        );

        String requestDTO = om.writeValueAsString(bodyList);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "message")
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

        ErrorCode errorCode = ErrorCode.BAD_REQUEST;

        assertNotNull(response);
        assertEquals(errorCode.getHttpStatus().value(), response.errorCode());
        assertEquals(errorCode.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(2, response.errors().size());

        Map<String, String> validationMap = new HashMap<>();
        validationMap.put("queueName", "NotBlank");
        validationMap.put("messageCount", "Min");

        response.errors().forEach(v -> {
            String constraint = validationMap.getOrDefault(v.field(), null);

            assertNotNull(constraint);
            assertEquals(constraint, v.constraint());
        });
    }
}
