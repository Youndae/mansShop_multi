package com.example.moduletest.admin.failedData.usecase;

import com.example.moduleadmin.model.dto.failedData.out.FailedQueueDTO;
import com.example.moduleadmin.repository.PeriodSalesSummaryRepository;
import com.example.moduleadmin.repository.ProductSalesSummaryRepository;
import com.example.moduleadmin.usecase.failedData.AdminFailedDataReadUseCase;
import com.example.modulecart.model.dto.business.CartMemberDTO;
import com.example.modulecart.repository.CartRepository;
import com.example.modulecommon.fixture.CartFixture;
import com.example.modulecommon.fixture.ClassificationFixture;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.FallbackMapKey;
import com.example.moduleconfig.config.rabbitMQ.RabbitMQPrefix;
import com.example.moduleconfig.properties.FallbackProperties;
import com.example.moduleconfig.properties.RabbitMQProperties;
import com.example.moduleorder.model.dto.business.FailedOrderDTO;
import com.example.moduleorder.model.dto.business.ProductOrderDataDTO;
import com.example.moduleorder.model.dto.in.OrderProductDTO;
import com.example.moduleorder.model.dto.in.PaymentDTO;
import com.example.moduleorder.model.dto.rabbitMQ.OrderCartDTO;
import com.example.moduleorder.model.dto.rabbitMQ.OrderProductMessageDTO;
import com.example.moduleorder.model.dto.rabbitMQ.OrderProductSummaryDTO;
import com.example.moduleorder.model.dto.rabbitMQ.PeriodSummaryQueueDTO;
import com.example.moduleorder.repository.ProductOrderRepository;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import com.example.moduletest.ModuleTestApplication;
import com.example.moduleuser.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ModuleTestApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
public class AdminFailedDataReadUseCaseIT {

    @Autowired
    private AdminFailedDataReadUseCase adminFailedDataReadUseCase;

    @Autowired
    private MemberRepository memberRepository;

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
    private CartRepository cartRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQProperties rabbitMQProperties;

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private RedisTemplate<String, FailedOrderDTO> failedOrderRedisTemplate;

    @Autowired
    private FallbackProperties fallbackProperties;

    private String failedOrderKey;

    private String failedOrderMessageKey;

    private Member member;

    private Product product;

    private List<ProductOption> productOptionList;

    private Cart memberCart;

    private List<CartDetail> memberCartDetailList;

    @BeforeEach
    void init() {
        MemberAndAuthFixtureDTO memberAndAuthFixtureDTO = MemberAndAuthFixture.createDefaultMember(1);
        member = memberAndAuthFixtureDTO.memberList().get(0);
        memberRepository.save(member);

        List<Classification> classificationList = ClassificationFixture.createClassifications();
        classificationRepository.saveAll(classificationList);

        product = ProductFixture.createProductFixtureList(1, classificationList.get(0)).get(0);
        productOptionList = product.getProductOptions();
        productRepository.save(product);
        productOptionRepository.saveAll(productOptionList);

        memberCart = CartFixture.createDefaultMemberCart(List.of(member), productOptionList).get(0);
        cartRepository.save(memberCart);

        memberCartDetailList = memberCart.getCartDetailList();

        failedOrderKey = fallbackProperties.getRedis().get(FallbackMapKey.ORDER.getKey()).getPrefix() + UUID.randomUUID();
        failedOrderMessageKey = fallbackProperties.getRedis().get(FallbackMapKey.ORDER_MESSAGE.getKey()).getPrefix() + UUID.randomUUID();
    }

    @AfterEach
    void cleanUp() {
        cartRepository.deleteAll();
        productOrderRepository.deleteAll();
        memberRepository.deleteAll();
        productSalesSummaryRepository.deleteAll();
        productOptionRepository.deleteAll();
        productRepository.deleteAll();
        classificationRepository.deleteAll();
        periodSalesSummaryRepository.deleteAll();

        failedOrderRedisTemplate.delete(failedOrderKey);
        failedOrderRedisTemplate.delete(failedOrderMessageKey);

        List<String> dlqNames = getDLQNames();
        dlqNames.forEach(v -> rabbitAdmin.purgeQueue(v, false));

        await()
                .atMost(60, TimeUnit.SECONDS)
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
    void getFailedMessageList() {
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
                    List<FailedQueueDTO> result = assertDoesNotThrow(() -> adminFailedDataReadUseCase.getFailedMessageList());

                    assertNotNull(result);
                    assertFalse(result.isEmpty());
                    fixtureFailedQueueDTO.forEach(v -> assertTrue(result.contains(v)));
                });
    }

    @Test
    @DisplayName(value = "DLQ 메시지 개수 조회. DLQ 메시지가 존재하지 않는 경우")
    void getFailedMessageListEmpty() {
        await()
                .atMost(60, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    List<FailedQueueDTO> result = assertDoesNotThrow(() -> adminFailedDataReadUseCase.getFailedMessageList());

                    assertNotNull(result);
                    assertTrue(result.isEmpty());
                });
    }

    @Test
    @DisplayName(value = "주문 실패 데이터 개수 Redis에서 조회. DB, RabbitMQ 장애로 인한 Redis 저장 데이터.")
    void getFailedOrderDataByRedis() {
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
        FailedOrderDTO failedOrderDTO = new FailedOrderDTO(paymentDTO, cartMemberDTO, LocalDateTime.now(), "message1");
        FailedOrderDTO failedOrderDTO2 = new FailedOrderDTO(paymentDTO, cartMemberDTO, LocalDateTime.now(), "message2");

        failedOrderRedisTemplate.opsForValue().set(failedOrderKey, failedOrderDTO);
        failedOrderRedisTemplate.opsForValue().set(failedOrderMessageKey, failedOrderDTO2);

        long result = assertDoesNotThrow(() -> adminFailedDataReadUseCase.getFailedOrderDataByRedis());

        assertEquals(2, result);
    }
}
