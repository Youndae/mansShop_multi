package com.example.moduletest.integration.order.controller;

import com.example.moduleadmin.repository.PeriodSalesSummaryRepository;
import com.example.moduleadmin.repository.ProductSalesSummaryRepository;
import com.example.moduleauth.repository.AuthRepository;
import com.example.moduleauth.repository.MemberRepository;
import com.example.modulecart.repository.CartDetailRepository;
import com.example.modulecart.repository.CartRepository;
import com.example.modulecommon.fixture.CartFixture;
import com.example.modulecommon.fixture.ClassificationFixture;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.dto.response.ResponseMessageDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.OrderStatus;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleorder.model.dto.in.OrderProductDTO;
import com.example.moduleorder.model.dto.in.PaymentDTO;
import com.example.moduleorder.model.vo.OrderItemVO;
import com.example.moduleorder.model.vo.PreOrderDataVO;
import com.example.moduleorder.repository.ProductOrderDetailRepository;
import com.example.moduleorder.repository.ProductOrderRepository;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import com.example.moduletest.ModuleTestApplication;
import com.example.moduletest.fixture.MemberPaymentMapDTO;
import com.example.moduletest.fixture.TokenFixture;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ModuleTestApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class OrderControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TokenFixture tokenFixture;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RedisTemplate<String, PreOrderDataVO> orderRedisTemplate;

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
    private CartRepository cartRepository;

    @Autowired
    private CartDetailRepository cartDetailRepository;

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Autowired
    private ProductOrderDetailRepository productOrderDetailRepository;

    @Autowired
    private PeriodSalesSummaryRepository periodSalesSummaryRepository;

    @Autowired
    private ProductSalesSummaryRepository productSalesSummaryRepository;

    @Value("#{jwt['token.access.header']}")
    private String accessHeader;

    @Value("#{jwt['token.refresh.header']}")
    private String refreshHeader;

    @Value("#{jwt['cookie.ino.header']}")
    private String inoHeader;

    @Value("#{jwt['cookie.cart.header']}")
    private String cartCookieHeader;

    private Member member;

    private Member noneMember;

    private Member anonymous;

    private String accessTokenValue;

    private String refreshTokenValue;

    private String inoValue;

    private Cart memberCart;

    private Cart noneMemberCart;

    private Cart anonymousCart;

    private List<Product> productList;

    private List<ProductOption> productOptionList;

    private Map<String, String> tokenMap;

    private static final String ANONYMOUS_CART_COOKIE = "anonymousCartCookieValue";

    private static final String ORDER_TOKEN = "memberOrderTokenValue";

    private static final String ORDER_TOKEN_HEADER = "order";

    private static final String URL_PREFIX = "/api/order/";

    @BeforeEach
    void init() {
        MemberAndAuthFixtureDTO memberAndAuthFixtureDTO = MemberAndAuthFixture.createDefaultMember(2);
        MemberAndAuthFixtureDTO adminFixture = MemberAndAuthFixture.createAdmin();
        MemberAndAuthFixtureDTO anonymousFixture = MemberAndAuthFixture.createAnonymous();
        List<Member> saveMemberList = new ArrayList<>(memberAndAuthFixtureDTO.memberList());
        saveMemberList.addAll(adminFixture.memberList());
        saveMemberList.addAll(anonymousFixture.memberList());
        List<Auth> saveAuthList = new ArrayList<>(memberAndAuthFixtureDTO.authList());
        saveAuthList.addAll(adminFixture.authList());
        saveAuthList.addAll(anonymousFixture.authList());
        memberRepository.saveAll(saveMemberList);
        authRepository.saveAll(saveAuthList);
        member = memberAndAuthFixtureDTO.memberList().get(0);
        noneMember = memberAndAuthFixtureDTO.memberList().get(1);
        anonymous = anonymousFixture.memberList().get(0);

        tokenMap = tokenFixture.createAndSaveAllToken(member);
        accessTokenValue = tokenMap.get(accessHeader);
        refreshTokenValue = tokenMap.get(refreshHeader);
        inoValue = tokenMap.get(inoHeader);

        List<Classification> classificationList = ClassificationFixture.createClassifications();
        classificationRepository.saveAll(classificationList);

        productList = ProductFixture.createProductFixtureList(3, classificationList.get(0));
        productOptionList = productList.stream()
                .flatMap(v ->
                        v.getProductOptions().stream()
                )
                .toList();
        productRepository.saveAll(productList);
        productOptionRepository.saveAll(productOptionList);

        memberCart = CartFixture.createDefaultMemberCart(List.of(member), productOptionList).get(0);
        noneMemberCart = CartFixture.createDefaultMemberCart(List.of(noneMember), productOptionList).get(0);
        anonymousCart = CartFixture.createSaveAnonymousCartMultipleOptions(productOptionList, anonymous, ANONYMOUS_CART_COOKIE);

        List<Cart> saveCartList = List.of(memberCart, noneMemberCart, anonymousCart);
        cartRepository.saveAll(saveCartList);
    }

    @AfterEach
    void cleanUP() {
        String accessKey = tokenMap.get("accessKey");
        String refreshKey = tokenMap.get("refreshKey");

        redisTemplate.delete(accessKey);
        redisTemplate.delete(refreshKey);
        orderRedisTemplate.delete(ORDER_TOKEN);

        productSalesSummaryRepository.deleteAll();
        periodSalesSummaryRepository.deleteAll();
        productOrderDetailRepository.deleteAll();
        productOrderRepository.deleteAll();
        cartDetailRepository.deleteAll();
        cartRepository.deleteAll();
        productOptionRepository.deleteAll();
        productRepository.deleteAll();
        classificationRepository.deleteAll();
        memberRepository.deleteAll();
        authRepository.deleteAll();
    }

    private MemberPaymentMapDTO getPaymentDataByCart(Cart cart) {
        int totalPrice = 0;
        int totalCount = 0;
        List<OrderProductDTO> orderProductFixtureList = new ArrayList<>();
        Map<String, Product> paymentProductMap = new HashMap<>();
        Map<String, Long> paymentProductSalesQuantityMap = new HashMap<>();
        Map<Long, ProductOption> paymentProductOptionMap = new HashMap<>();
        Map<Long, Long> paymentProductOptionStockMap = new HashMap<>();

        for(CartDetail detail : cart.getCartDetailList()) {
            Product productFixture = detail.getProductOption().getProduct();
            int thisPrice = (int) (productFixture.getProductPrice() * (1 - ((double) productFixture.getProductDiscount() / 100)));
            int thisTotalPrice = detail.getCartCount() * thisPrice;
            totalPrice += thisTotalPrice;
            totalCount += detail.getCartCount();
            paymentProductMap.put(
                    productFixture.getId(),
                    paymentProductMap.getOrDefault(productFixture.getId(), productFixture)
            );
            paymentProductSalesQuantityMap.put(
                    productFixture.getId(),
                    paymentProductSalesQuantityMap.getOrDefault(productFixture.getId(), 0L) + detail.getCartCount()
            );
            paymentProductOptionMap.put(
                    detail.getProductOption().getId(),
                    paymentProductOptionMap.getOrDefault(detail.getProductOption().getId(), detail.getProductOption())
            );
            paymentProductOptionStockMap.put(
                    detail.getProductOption().getId(),
                    paymentProductOptionStockMap.getOrDefault(detail.getProductOption().getId(), 0L) + detail.getCartCount()
            );

            orderProductFixtureList.add(
                    new OrderProductDTO(
                            detail.getProductOption().getId(),
                            productFixture.getProductName(),
                            productFixture.getId(),
                            detail.getCartCount(),
                            thisTotalPrice
                    )
            );
        }

        return new MemberPaymentMapDTO(
                totalPrice,
                totalCount,
                orderProductFixtureList,
                paymentProductMap,
                paymentProductSalesQuantityMap,
                paymentProductOptionMap,
                paymentProductOptionStockMap
        );
    }

    private void verifyPaymentByCartResult(MemberPaymentMapDTO paymentFixtureDTO, Long cartId, PaymentDTO paymentDTO, Member paymentMember) {
        List<ProductOrder> saveOrderList = productOrderRepository.findAll();
        assertNotNull(saveOrderList);
        assertFalse(saveOrderList.isEmpty());
        assertEquals(1, saveOrderList.size());

        ProductOrder saveOrder = saveOrderList.get(0);
        assertEquals(paymentMember.getUserId(), saveOrder.getMember().getUserId());
        assertEquals(paymentDTO.recipient(), saveOrder.getRecipient());
        assertEquals(paymentDTO.phone(), saveOrder.getOrderPhone());
        assertEquals(paymentDTO.address(), saveOrder.getOrderAddress());
        assertEquals(paymentDTO.totalPrice(), saveOrder.getOrderTotalPrice());
        assertEquals(paymentDTO.deliveryFee(), saveOrder.getDeliveryFee());
        assertEquals(paymentDTO.paymentType(), saveOrder.getPaymentType());
        assertEquals(OrderStatus.ORDER.getStatusStr(), saveOrder.getOrderStat());
        assertEquals(paymentFixtureDTO.totalCount(), saveOrder.getProductCount());

        List<ProductOrderDetail> saveOrderDetails = productOrderDetailRepository.findAll();
        assertEquals(paymentDTO.orderProduct().size(), saveOrderDetails.size());

        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    Cart checkMemberCart = cartRepository.findById(cartId).orElse(null);
                    assertNull(checkMemberCart);

                    List<ProductOption> patchProductOptionList = productOptionRepository.findAll();
                    for(ProductOption patchProduct : patchProductOptionList) {
                        Product mapData = paymentFixtureDTO.paymentProductMap().getOrDefault(patchProduct.getProduct().getId(), null);
                        Long addSalesQuantity = paymentFixtureDTO.paymentProductSalesQuantityMap().getOrDefault(patchProduct.getProduct().getId(), null);
                        assertNotNull(mapData);
                        assertNotNull(addSalesQuantity);
                        assertEquals(mapData.getProductSalesQuantity() + addSalesQuantity, patchProduct.getProduct().getProductSalesQuantity());

                        ProductOption optionMapData = paymentFixtureDTO.paymentProductOptionMap().getOrDefault(patchProduct.getId(), null);
                        Long subStock = paymentFixtureDTO.paymentProductOptionStockMap().getOrDefault(patchProduct.getId(), null);
                        assertNotNull(optionMapData);
                        assertNotNull(subStock);
                        long stock = optionMapData.getStock() - subStock;
                        if(stock < 0)
                            stock = 0;
                        assertEquals(stock, patchProduct.getStock());
                    }

                    List<PeriodSalesSummary> periodSalesSummary = periodSalesSummaryRepository.findAll();
                    assertNotNull(periodSalesSummary);
                    assertFalse(periodSalesSummary.isEmpty());
                    assertEquals(1, periodSalesSummary.size());
                    PeriodSalesSummary periodSummary = periodSalesSummary.get(0);
                    assertEquals(paymentFixtureDTO.totalPrice(), periodSummary.getCardTotal());
                    assertEquals(1, periodSummary.getOrderQuantity());
                    assertEquals(0, periodSummary.getCashTotal());
                    assertEquals(paymentFixtureDTO.totalPrice(), periodSummary.getSales());
                    assertEquals(paymentFixtureDTO.totalCount(), periodSummary.getSalesQuantity());

                    List<ProductSalesSummary> productSalesSummary = productSalesSummaryRepository.findAll();
                    assertNotNull(productSalesSummary);
                    assertFalse(productSalesSummary.isEmpty());
                    assertEquals(productOptionList.size(), productSalesSummary.size());

                    Map<Long, OrderProductDTO> productSummaryMap = paymentDTO.orderProduct().stream()
                            .collect(Collectors.toMap(
                                    OrderProductDTO::getOptionId,
                                    dto -> dto
                            ));

                    for(ProductSalesSummary summary : productSalesSummary) {
                        OrderProductDTO mapData = productSummaryMap.getOrDefault(summary.getProductOption().getId(), new OrderProductDTO());
                        assertEquals(mapData.getDetailPrice(), summary.getSales());
                        assertEquals(mapData.getDetailCount(), summary.getSalesQuantity());
                        assertEquals(1, summary.getOrderQuantity());
                    }
                });
    }

    @Test
    @DisplayName(value = "회원의 결제 완료 이후 주문 데이터 처리. 장바구니를 통한 구매인 경우")
    void paymentByMemberCart() throws Exception {
        MemberPaymentMapDTO paymentFixtureDTO = getPaymentDataByCart(memberCart);
        List<OrderProductDTO> orderProductFixutreList = paymentFixtureDTO.orderProductFixtureList();
        List<OrderItemVO> orderItemVOList = orderProductFixutreList.stream()
                .map(v -> new OrderItemVO(
                        v.getProductId(),
                        v.getOptionId(),
                        v.getDetailCount(),
                        v.getDetailPrice()
                ))
                .toList();
        PreOrderDataVO cachingOrderDataVO = new PreOrderDataVO(member.getUserId(), orderItemVOList, paymentFixtureDTO.totalPrice());
        orderRedisTemplate.opsForValue().set(ORDER_TOKEN, cachingOrderDataVO);
        Long cartId = memberCart.getId();

        PaymentDTO paymentDTO = new PaymentDTO(
                member.getUserName(),
                member.getPhone().replaceAll("-", ""),
                member.getUserName() + "'s memo",
                member.getUserName() + "'s address",
                orderProductFixutreList,
                paymentFixtureDTO.totalPrice() < 100000 ? 3500 : 0,
                paymentFixtureDTO.totalPrice(),
                "card",
                "cart"
        );
        String requestDTO = om.writeValueAsString(paymentDTO);
        MvcResult result = mockMvc.perform(post(URL_PREFIX)
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
                        .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ResponseMessageDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(Result.OK.getResultKey(), response.message());

        verifyPaymentByCartResult(paymentFixtureDTO, cartId, paymentDTO, member);
    }

    @Test
    @DisplayName(value = "비회원의 결제 완료 이후 주문 데이터 처리. 장바구니를 통한 구매인 경우")
    void paymentByAnonymousCart() throws Exception {
        MemberPaymentMapDTO paymentFixtureDTO = getPaymentDataByCart(anonymousCart);
        List<OrderProductDTO> orderProductFixutreList = paymentFixtureDTO.orderProductFixtureList();
        List<OrderItemVO> orderItemVOList = orderProductFixutreList.stream()
                .map(v -> new OrderItemVO(
                        v.getProductId(),
                        v.getOptionId(),
                        v.getDetailCount(),
                        v.getDetailPrice()
                ))
                .toList();
        PreOrderDataVO cachingOrderDataVO = new PreOrderDataVO(anonymous.getUserId(), orderItemVOList, paymentFixtureDTO.totalPrice());
        orderRedisTemplate.opsForValue().set(ORDER_TOKEN, cachingOrderDataVO);
        Long cartId = anonymousCart.getId();

        PaymentDTO paymentDTO = new PaymentDTO(
                "anonymousName",
                "01013132424",
                "anonymous memo",
                "anonymous address",
                orderProductFixutreList,
                paymentFixtureDTO.totalPrice() < 100000 ? 3500 : 0,
                paymentFixtureDTO.totalPrice(),
                "card",
                "cart"
        );
        String requestDTO = om.writeValueAsString(paymentDTO);
        MvcResult result = mockMvc.perform(post(URL_PREFIX)
                        .cookie(new Cookie(cartCookieHeader, ANONYMOUS_CART_COOKIE))
                        .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        ResponseMessageDTO response = om.readValue(
                content,
                new TypeReference<>() {}
        );

        assertNotNull(response);
        assertEquals(Result.OK.getResultKey(), response.message());

        verifyPaymentByCartResult(paymentFixtureDTO, cartId, paymentDTO, anonymous);
    }
}
