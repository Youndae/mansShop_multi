package com.example.moduleapi.controller.order;

import com.example.moduleadmin.repository.PeriodSalesSummaryRepository;
import com.example.moduleadmin.repository.ProductSalesSummaryRepository;
import com.example.moduleapi.ModuleApiApplication;
import com.example.moduleapi.config.exception.ExceptionEntity;
import com.example.moduleapi.config.exception.ValidationError;
import com.example.moduleapi.config.exception.ValidationExceptionEntity;
import com.example.moduleapi.fixture.TokenFixture;
import com.example.moduleapi.fixture.dto.MemberPaymentMapDTO;
import com.example.modulecart.repository.CartDetailRepository;
import com.example.modulecart.repository.CartRepository;
import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.customException.CustomNotFoundException;
import com.example.modulecommon.fixture.CartFixture;
import com.example.modulecommon.fixture.ClassificationFixture;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.model.enumuration.OrderStatus;
import com.example.modulecommon.utils.PhoneNumberUtils;
import com.example.moduleconfig.properties.CookieProperties;
import com.example.moduleconfig.properties.TokenProperties;
import com.example.moduleorder.model.dto.business.OrderDataDTO;
import com.example.moduleorder.model.dto.in.OrderProductDTO;
import com.example.moduleorder.model.dto.in.OrderProductRequestDTO;
import com.example.moduleorder.model.dto.in.PaymentDTO;
import com.example.moduleorder.model.dto.out.OrderDataResponseDTO;
import com.example.moduleorder.model.enumuration.OrderPaymentType;
import com.example.moduleorder.model.enumuration.OrderRequestType;
import com.example.moduleorder.model.vo.OrderItemVO;
import com.example.moduleorder.model.vo.PreOrderDataVO;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest(classes = ModuleApiApplication.class)
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

    @Autowired
    private TokenProperties tokenProperties;

    @Autowired
    private CookieProperties cookieProperties;

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

    private static final ErrorCode BAD_REQUEST_ERROR_CODE = ErrorCode.BAD_REQUEST;

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
        accessTokenValue = tokenMap.get(tokenProperties.getAccess().getHeader());
        refreshTokenValue = tokenMap.get(tokenProperties.getRefresh().getHeader());
        inoValue = tokenMap.get(cookieProperties.getIno().getHeader());

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

        productSalesSummaryRepository.deleteAllInBatch();
        periodSalesSummaryRepository.deleteAllInBatch();
        productOrderDetailRepository.deleteAllInBatch();
        productOrderRepository.deleteAllInBatch();
        cartDetailRepository.deleteAllInBatch();
        cartRepository.deleteAllInBatch();
        productOptionRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        classificationRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        authRepository.deleteAllInBatch();
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
        assertEquals(PhoneNumberUtils.format(paymentDTO.phone()), saveOrder.getOrderPhone());
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
                OrderPaymentType.CARD.getType(),
                OrderRequestType.CART.getType()
        );
        String requestDTO = om.writeValueAsString(paymentDTO);
        mockMvc.perform(post(URL_PREFIX)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isNoContent())
                .andReturn();

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
                OrderPaymentType.CARD.getType(),
                OrderRequestType.CART.getType()
        );
        String requestDTO = om.writeValueAsString(paymentDTO);
        mockMvc.perform(post(URL_PREFIX)
                        .cookie(new Cookie(cookieProperties.getCart().getHeader(), ANONYMOUS_CART_COOKIE))
                        .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isNoContent())
                .andReturn();

        verifyPaymentByCartResult(paymentFixtureDTO, cartId, paymentDTO, anonymous);
    }

    @Test
    @DisplayName(value = "회원의 결제 완료 이후 주문 데이터 처리. 장바구니를 통한 구매인 경우. 수령인 데이터가 null인 경우")
    void paymentByMemberCartValidationRecipientIsNull() throws Exception {
        MemberPaymentMapDTO paymentFixtureDTO = getPaymentDataByCart(memberCart);
        List<OrderProductDTO> orderProductFixutreList = paymentFixtureDTO.orderProductFixtureList();

        PaymentDTO paymentDTO = new PaymentDTO(
                null,
                member.getPhone().replaceAll("-", ""),
                member.getUserName() + "'s memo",
                member.getUserName() + "'s address",
                orderProductFixutreList,
                paymentFixtureDTO.totalPrice() < 100000 ? 3500 : 0,
                paymentFixtureDTO.totalPrice(),
                OrderPaymentType.CARD.getType(),
                OrderRequestType.CART.getType()
        );
        String requestDTO = om.writeValueAsString(paymentDTO);
        MvcResult result = mockMvc.perform(post(URL_PREFIX)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                                .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
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

        assertEquals("recipient", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원의 결제 완료 이후 주문 데이터 처리. 장바구니를 통한 구매인 경우. 수령인 데이터가 Blank인 경우")
    void paymentByMemberCartValidationRecipientIsBlank() throws Exception {
        MemberPaymentMapDTO paymentFixtureDTO = getPaymentDataByCart(memberCart);
        List<OrderProductDTO> orderProductFixutreList = paymentFixtureDTO.orderProductFixtureList();

        PaymentDTO paymentDTO = new PaymentDTO(
                "",
                member.getPhone().replaceAll("-", ""),
                member.getUserName() + "'s memo",
                member.getUserName() + "'s address",
                orderProductFixutreList,
                paymentFixtureDTO.totalPrice() < 100000 ? 3500 : 0,
                paymentFixtureDTO.totalPrice(),
                OrderPaymentType.CARD.getType(),
                OrderRequestType.CART.getType()
        );
        String requestDTO = om.writeValueAsString(paymentDTO);
        MvcResult result = mockMvc.perform(post(URL_PREFIX)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
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

        assertEquals("recipient", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원의 결제 완료 이후 주문 데이터 처리. 장바구니를 통한 구매인 경우. 연락처가 null인 경우")
    void paymentByMemberCartValidationWrongPhoneIsNull() throws Exception {
        MemberPaymentMapDTO paymentFixtureDTO = getPaymentDataByCart(memberCart);
        List<OrderProductDTO> orderProductFixutreList = paymentFixtureDTO.orderProductFixtureList();

        PaymentDTO paymentDTO = new PaymentDTO(
                member.getUserName(),
                null,
                member.getUserName() + "'s memo",
                member.getUserName() + "'s address",
                orderProductFixutreList,
                paymentFixtureDTO.totalPrice() < 100000 ? 3500 : 0,
                paymentFixtureDTO.totalPrice(),
                OrderPaymentType.CARD.getType(),
                OrderRequestType.CART.getType()
        );
        String requestDTO = om.writeValueAsString(paymentDTO);
        MvcResult result = mockMvc.perform(post(URL_PREFIX)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
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

        assertEquals("phone", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원의 결제 완료 이후 주문 데이터 처리. 장바구니를 통한 구매인 경우. 연락처가 Blank인 경우")
    void paymentByMemberCartValidationWrongPhoneIsBlank() throws Exception {
        MemberPaymentMapDTO paymentFixtureDTO = getPaymentDataByCart(memberCart);
        List<OrderProductDTO> orderProductFixutreList = paymentFixtureDTO.orderProductFixtureList();

        PaymentDTO paymentDTO = new PaymentDTO(
                member.getUserName(),
                "",
                member.getUserName() + "'s memo",
                member.getUserName() + "'s address",
                orderProductFixutreList,
                paymentFixtureDTO.totalPrice() < 100000 ? 3500 : 0,
                paymentFixtureDTO.totalPrice(),
                OrderPaymentType.CARD.getType(),
                OrderRequestType.CART.getType()
        );
        String requestDTO = om.writeValueAsString(paymentDTO);
        MvcResult result = mockMvc.perform(post(URL_PREFIX)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
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

        List<String> validationConstraintList = List.of("NotBlank", "Pattern");

        response.errors().forEach(v -> validationConstraintList.contains(v.constraint()));
    }

    @Test
    @DisplayName(value = "회원의 결제 완료 이후 주문 데이터 처리. 장바구니를 통한 구매인 경우. 연락처에서 하이픈이 같이 전달되는 경우")
    void paymentByMemberCartValidationWrongPhonePatternIncludeHyphen() throws Exception {
        MemberPaymentMapDTO paymentFixtureDTO = getPaymentDataByCart(memberCart);
        List<OrderProductDTO> orderProductFixutreList = paymentFixtureDTO.orderProductFixtureList();

        PaymentDTO paymentDTO = new PaymentDTO(
                member.getUserName(),
                member.getPhone(),
                member.getUserName() + "'s memo",
                member.getUserName() + "'s address",
                orderProductFixutreList,
                paymentFixtureDTO.totalPrice() < 100000 ? 3500 : 0,
                paymentFixtureDTO.totalPrice(),
                OrderPaymentType.CARD.getType(),
                OrderRequestType.CART.getType()
        );
        String requestDTO = om.writeValueAsString(paymentDTO);
        MvcResult result = mockMvc.perform(post(URL_PREFIX)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
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

        assertEquals("phone", responseObj.field());
        assertEquals("Pattern", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원의 결제 완료 이후 주문 데이터 처리. 장바구니를 통한 구매인 경우. 연락처가 10자리 미만인 경우")
    void paymentByMemberCartValidationWrongPhonePatternToShort() throws Exception {
        MemberPaymentMapDTO paymentFixtureDTO = getPaymentDataByCart(memberCart);
        List<OrderProductDTO> orderProductFixutreList = paymentFixtureDTO.orderProductFixtureList();

        PaymentDTO paymentDTO = new PaymentDTO(
                member.getUserName(),
                "01012123",
                member.getUserName() + "'s memo",
                member.getUserName() + "'s address",
                orderProductFixutreList,
                paymentFixtureDTO.totalPrice() < 100000 ? 3500 : 0,
                paymentFixtureDTO.totalPrice(),
                OrderPaymentType.CARD.getType(),
                OrderRequestType.CART.getType()
        );
        String requestDTO = om.writeValueAsString(paymentDTO);
        MvcResult result = mockMvc.perform(post(URL_PREFIX)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
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

        assertEquals("phone", responseObj.field());
        assertEquals("Pattern", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원의 결제 완료 이후 주문 데이터 처리. 장바구니를 통한 구매인 경우. 연락처가 11자리 초과인 경우")
    void paymentByMemberCartValidationWrongPhonePatternToLong() throws Exception {
        MemberPaymentMapDTO paymentFixtureDTO = getPaymentDataByCart(memberCart);
        List<OrderProductDTO> orderProductFixutreList = paymentFixtureDTO.orderProductFixtureList();

        PaymentDTO paymentDTO = new PaymentDTO(
                member.getUserName(),
                "010123412345",
                member.getUserName() + "'s memo",
                member.getUserName() + "'s address",
                orderProductFixutreList,
                paymentFixtureDTO.totalPrice() < 100000 ? 3500 : 0,
                paymentFixtureDTO.totalPrice(),
                OrderPaymentType.CARD.getType(),
                OrderRequestType.CART.getType()
        );
        String requestDTO = om.writeValueAsString(paymentDTO);
        MvcResult result = mockMvc.perform(post(URL_PREFIX)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
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

        assertEquals("phone", responseObj.field());
        assertEquals("Pattern", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원의 결제 완료 이후 주문 데이터 처리. 장바구니를 통한 구매인 경우. 배송지가 null인 경우")
    void paymentByMemberCartValidationAddressIsNull() throws Exception {
        MemberPaymentMapDTO paymentFixtureDTO = getPaymentDataByCart(memberCart);
        List<OrderProductDTO> orderProductFixutreList = paymentFixtureDTO.orderProductFixtureList();

        PaymentDTO paymentDTO = new PaymentDTO(
                member.getUserName(),
                member.getPhone().replaceAll("-", ""),
                member.getUserName() + "'s memo",
                null,
                orderProductFixutreList,
                paymentFixtureDTO.totalPrice() < 100000 ? 3500 : 0,
                paymentFixtureDTO.totalPrice(),
                OrderPaymentType.CARD.getType(),
                OrderRequestType.CART.getType()
        );
        String requestDTO = om.writeValueAsString(paymentDTO);
        MvcResult result = mockMvc.perform(post(URL_PREFIX)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
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

        assertEquals("address", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원의 결제 완료 이후 주문 데이터 처리. 장바구니를 통한 구매인 경우. 배송지가 Blank인 경우")
    void paymentByMemberCartValidationAddressIsBlank() throws Exception {
        MemberPaymentMapDTO paymentFixtureDTO = getPaymentDataByCart(memberCart);
        List<OrderProductDTO> orderProductFixutreList = paymentFixtureDTO.orderProductFixtureList();

        PaymentDTO paymentDTO = new PaymentDTO(
                member.getUserName(),
                member.getPhone().replaceAll("-", ""),
                member.getUserName() + "'s memo",
                "",
                orderProductFixutreList,
                paymentFixtureDTO.totalPrice() < 100000 ? 3500 : 0,
                paymentFixtureDTO.totalPrice(),
                OrderPaymentType.CARD.getType(),
                OrderRequestType.CART.getType()
        );
        String requestDTO = om.writeValueAsString(paymentDTO);
        MvcResult result = mockMvc.perform(post(URL_PREFIX)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
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

        assertEquals("address", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원의 결제 완료 이후 주문 데이터 처리. 장바구니를 통한 구매인 경우. 배송비가 음수인 경우")
    void paymentByMemberCartValidationDeliveryFeeIsNegativeNumber() throws Exception {
        MemberPaymentMapDTO paymentFixtureDTO = getPaymentDataByCart(memberCart);
        List<OrderProductDTO> orderProductFixutreList = paymentFixtureDTO.orderProductFixtureList();

        PaymentDTO paymentDTO = new PaymentDTO(
                member.getUserName(),
                member.getPhone().replaceAll("-", ""),
                member.getUserName() + "'s memo",
                member.getUserName() + "'s address",
                orderProductFixutreList,
                -1,
                paymentFixtureDTO.totalPrice(),
                OrderPaymentType.CARD.getType(),
                OrderRequestType.CART.getType()
        );
        String requestDTO = om.writeValueAsString(paymentDTO);
        MvcResult result = mockMvc.perform(post(URL_PREFIX)
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                                .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
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

        assertEquals("deliveryFee", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원의 결제 완료 이후 주문 데이터 처리. 장바구니를 통한 구매인 경우. 총 가격이 음수인 경우")
    void paymentByMemberCartValidationTotalPriceIsNegativeNumber() throws Exception {
        MemberPaymentMapDTO paymentFixtureDTO = getPaymentDataByCart(memberCart);
        List<OrderProductDTO> orderProductFixutreList = paymentFixtureDTO.orderProductFixtureList();

        PaymentDTO paymentDTO = new PaymentDTO(
                member.getUserName(),
                member.getPhone().replaceAll("-", ""),
                member.getUserName() + "'s memo",
                member.getUserName() + "'s address",
                orderProductFixutreList,
                paymentFixtureDTO.totalPrice() < 100000 ? 3500 : 0,
                -1,
                OrderPaymentType.CARD.getType(),
                OrderRequestType.CART.getType()
        );
        String requestDTO = om.writeValueAsString(paymentDTO);
        MvcResult result = mockMvc.perform(post(URL_PREFIX)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
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

        assertEquals("totalPrice", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원의 결제 완료 이후 주문 데이터 처리. 장바구니를 통한 구매인 경우. 결제 타입이 null인 경우")
    void paymentByMemberCartValidationPaymentTypeIsNull() throws Exception {
        MemberPaymentMapDTO paymentFixtureDTO = getPaymentDataByCart(memberCart);
        List<OrderProductDTO> orderProductFixutreList = paymentFixtureDTO.orderProductFixtureList();

        PaymentDTO paymentDTO = new PaymentDTO(
                member.getUserName(),
                member.getPhone().replaceAll("-", ""),
                member.getUserName() + "'s memo",
                member.getUserName() + "'s address",
                orderProductFixutreList,
                paymentFixtureDTO.totalPrice() < 100000 ? 3500 : 0,
                paymentFixtureDTO.totalPrice(),
                null,
                OrderRequestType.CART.getType()
        );
        String requestDTO = om.writeValueAsString(paymentDTO);
        MvcResult result = mockMvc.perform(post(URL_PREFIX)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
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

        assertEquals("paymentType", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원의 결제 완료 이후 주문 데이터 처리. 장바구니를 통한 구매인 경우. 결제 타입이 Blank인 경우")
    void paymentByMemberCartValidationPaymentTypeIsBlank() throws Exception {
        MemberPaymentMapDTO paymentFixtureDTO = getPaymentDataByCart(memberCart);
        List<OrderProductDTO> orderProductFixutreList = paymentFixtureDTO.orderProductFixtureList();

        PaymentDTO paymentDTO = new PaymentDTO(
                member.getUserName(),
                member.getPhone().replaceAll("-", ""),
                member.getUserName() + "'s memo",
                member.getUserName() + "'s address",
                orderProductFixutreList,
                paymentFixtureDTO.totalPrice() < 100000 ? 3500 : 0,
                paymentFixtureDTO.totalPrice(),
                "",
                OrderRequestType.CART.getType()
        );
        String requestDTO = om.writeValueAsString(paymentDTO);
        MvcResult result = mockMvc.perform(post(URL_PREFIX)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
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

        assertEquals("paymentType", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원의 결제 완료 이후 주문 데이터 처리. 장바구니를 통한 구매인 경우. 결제 타입이 잘못된 경우")
    void paymentByMemberCartValidationPaymentTypeIsWrong() throws Exception {
        MemberPaymentMapDTO paymentFixtureDTO = getPaymentDataByCart(memberCart);
        List<OrderProductDTO> orderProductFixutreList = paymentFixtureDTO.orderProductFixtureList();

        PaymentDTO paymentDTO = new PaymentDTO(
                member.getUserName(),
                member.getPhone().replaceAll("-", ""),
                member.getUserName() + "'s memo",
                member.getUserName() + "'s address",
                orderProductFixutreList,
                paymentFixtureDTO.totalPrice() < 100000 ? 3500 : 0,
                paymentFixtureDTO.totalPrice(),
                OrderRequestType.CART.getType(),
                OrderRequestType.CART.getType()
        );
        String requestDTO = om.writeValueAsString(paymentDTO);
        MvcResult result = mockMvc.perform(post(URL_PREFIX)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
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
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원의 결제 완료 이후 주문 데이터 처리. 장바구니를 통한 구매인 경우. 주문 요청 방식이 null인 경우")
    void paymentByMemberCartValidationOrderTypeIsNull() throws Exception {
        MemberPaymentMapDTO paymentFixtureDTO = getPaymentDataByCart(memberCart);
        List<OrderProductDTO> orderProductFixutreList = paymentFixtureDTO.orderProductFixtureList();

        PaymentDTO paymentDTO = new PaymentDTO(
                member.getUserName(),
                member.getPhone().replaceAll("-", ""),
                member.getUserName() + "'s memo",
                member.getUserName() + "'s address",
                orderProductFixutreList,
                paymentFixtureDTO.totalPrice() < 100000 ? 3500 : 0,
                paymentFixtureDTO.totalPrice(),
                OrderPaymentType.CARD.getType(),
                null
        );
        String requestDTO = om.writeValueAsString(paymentDTO);
        MvcResult result = mockMvc.perform(post(URL_PREFIX)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
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

        assertEquals("orderType", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원의 결제 완료 이후 주문 데이터 처리. 장바구니를 통한 구매인 경우. 주문 요청 방식이 Blank인 경우")
    void paymentByMemberCartValidationOrderTypeIsBlank() throws Exception {
        MemberPaymentMapDTO paymentFixtureDTO = getPaymentDataByCart(memberCart);
        List<OrderProductDTO> orderProductFixutreList = paymentFixtureDTO.orderProductFixtureList();

        PaymentDTO paymentDTO = new PaymentDTO(
                member.getUserName(),
                member.getPhone().replaceAll("-", ""),
                member.getUserName() + "'s memo",
                member.getUserName() + "'s address",
                orderProductFixutreList,
                paymentFixtureDTO.totalPrice() < 100000 ? 3500 : 0,
                paymentFixtureDTO.totalPrice(),
                OrderPaymentType.CARD.getType(),
                ""
        );
        String requestDTO = om.writeValueAsString(paymentDTO);
        MvcResult result = mockMvc.perform(post(URL_PREFIX)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
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

        assertEquals("orderType", responseObj.field());
        assertEquals("NotBlank", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원의 결제 완료 이후 주문 데이터 처리. 장바구니를 통한 구매인 경우. 주문 요청 방식이 잘못된 경우")
    void paymentByMemberCartValidationOrderTypeIsWrong() throws Exception {
        MemberPaymentMapDTO paymentFixtureDTO = getPaymentDataByCart(memberCart);
        List<OrderProductDTO> orderProductFixutreList = paymentFixtureDTO.orderProductFixtureList();

        PaymentDTO paymentDTO = new PaymentDTO(
                member.getUserName(),
                member.getPhone().replaceAll("-", ""),
                member.getUserName() + "'s memo",
                member.getUserName() + "'s address",
                orderProductFixutreList,
                paymentFixtureDTO.totalPrice() < 100000 ? 3500 : 0,
                paymentFixtureDTO.totalPrice(),
                OrderPaymentType.CARD.getType(),
                OrderPaymentType.CARD.getType()
        );
        String requestDTO = om.writeValueAsString(paymentDTO);
        MvcResult result = mockMvc.perform(post(URL_PREFIX)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
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
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "회원의 결제 완료 이후 주문 데이터 처리. 장바구니를 통한 구매인 경우. 상품 정보를 제외한 모든 주문 데이터가 잘못된 경우")
    void paymentByMemberCartValidationAllParameterIsWrong() throws Exception {
        MemberPaymentMapDTO paymentFixtureDTO = getPaymentDataByCart(memberCart);
        List<OrderProductDTO> orderProductFixutreList = paymentFixtureDTO.orderProductFixtureList();

        // recipient: null (NotBlank)
        // phone: Hyphen 포함 (Pattern)
        // address: null (NotBlank)
        // deliveryFee: -1 (Min)
        // totalPrice: -1 (Min)
        // paymentType: null (NotBlank)
        // orderType: null (NotBlank)
        PaymentDTO paymentDTO = new PaymentDTO(
                null,
                member.getPhone(),
                member.getUserName() + "'s memo",
                null,
                orderProductFixutreList,
                -1,
                -1,
                null,
                null
        );
        String requestDTO = om.writeValueAsString(paymentDTO);
        MvcResult result = mockMvc.perform(post(URL_PREFIX)
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
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

        assertEquals(7, response.errors().size());

        Map<String, String> validationMap = new HashMap<>();
        validationMap.put("recipient", "NotBlank");
        validationMap.put("phone", "Pattern");
        validationMap.put("address", "NotBlank");
        validationMap.put("deliveryFee", "Min");
        validationMap.put("totalPrice", "Min");
        validationMap.put("paymentType", "NotBlank");
        validationMap.put("orderType", "NotBlank");

        response.errors().forEach(v -> {
            String constraint = validationMap.getOrDefault(v.field(), null);

            assertNotNull(constraint);
            assertEquals(constraint, v.constraint());
        });
    }

    @Test
    @DisplayName(value = "회원이 상품 상세 페이지에서 결제 요청 시 상품 결제 정보 조회")
    void orderProductByMember() throws Exception {
        Product fixture = productList.get(0);
        List<OrderDataDTO> responseFixture = new ArrayList<>();
        List<OrderItemVO> redisFixtureFieldList = new ArrayList<>();
        int totalPrice = 0;
        for(ProductOption option : fixture.getProductOptions()) {
            int price = (int) (fixture.getProductPrice() * (1 - ((double) fixture.getProductDiscount() / 100))) * 3;
            totalPrice += price;
            responseFixture.add(
                    new OrderDataDTO(
                            fixture.getId(),
                            option.getId(),
                            fixture.getProductName(),
                            option.getSize(),
                            option.getColor(),
                            3,
                            price
                    )
            );

            redisFixtureFieldList.add(
                    new OrderItemVO(
                            fixture.getId(),
                            option.getId(),
                            3,
                            price
                    )
            );
        }
        PreOrderDataVO redisFixture = new PreOrderDataVO(member.getUserId(), redisFixtureFieldList, totalPrice);
        List<OrderProductRequestDTO> orderProductDTO = fixture.getProductOptions()
                .stream()
                .map(v -> new OrderProductRequestDTO(v.getId(), 3))
                .toList();
        String requestDTO = om.writeValueAsString(orderProductDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        Map<String, String> cookieMap = tokenFixture.getCookieMap(result);
        OrderDataResponseDTO response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(totalPrice, response.totalPrice());
        assertEquals(responseFixture.size(), response.orderData().size());

        response.orderData().forEach(v -> assertTrue(responseFixture.contains(v)));
        assertNotNull(cookieMap);
        assertEquals(1, cookieMap.size());
        String orderTokenCookieValue = cookieMap.getOrDefault("order", null);
        assertNotNull(orderTokenCookieValue);

        PreOrderDataVO redisOrderValue = orderRedisTemplate.opsForValue().get(orderTokenCookieValue);
        assertNotNull(redisOrderValue);

        assertEquals(redisFixture, redisOrderValue);

        redisTemplate.delete(orderTokenCookieValue);
    }

    @Test
    @DisplayName(value = "비회원이 상품 상세 페이지에서 결제 요청 시 상품 결제 정보 조회")
    void orderProductByAnonymous() throws Exception {
        Product fixture = productList.get(0);
        List<OrderDataDTO> responseFixture = new ArrayList<>();
        List<OrderItemVO> redisFixtureFieldList = new ArrayList<>();
        int totalPrice = 0;
        for(ProductOption option : fixture.getProductOptions()) {
            int price = (int) (fixture.getProductPrice() * (1 - ((double) fixture.getProductDiscount() / 100))) * 3;
            totalPrice += price;
            responseFixture.add(
                    new OrderDataDTO(
                            fixture.getId(),
                            option.getId(),
                            fixture.getProductName(),
                            option.getSize(),
                            option.getColor(),
                            3,
                            price
                    )
            );

            redisFixtureFieldList.add(
                    new OrderItemVO(
                            fixture.getId(),
                            option.getId(),
                            3,
                            price
                    )
            );
        }
        PreOrderDataVO redisFixture = new PreOrderDataVO(anonymous.getUserId(), redisFixtureFieldList, totalPrice);
        List<OrderProductRequestDTO> orderProductDTO = fixture.getProductOptions()
                .stream()
                .map(v -> new OrderProductRequestDTO(v.getId(), 3))
                .toList();
        String requestDTO = om.writeValueAsString(orderProductDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        Map<String, String> cookieMap = tokenFixture.getCookieMap(result);
        OrderDataResponseDTO response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(totalPrice, response.totalPrice());
        assertEquals(responseFixture.size(), response.orderData().size());

        response.orderData().forEach(v -> assertTrue(responseFixture.contains(v)));
        assertNotNull(cookieMap);
        assertEquals(1, cookieMap.size());
        String orderTokenCookieValue = cookieMap.getOrDefault("order", null);
        assertNotNull(orderTokenCookieValue);

        PreOrderDataVO redisOrderValue = orderRedisTemplate.opsForValue().get(orderTokenCookieValue);
        assertNotNull(redisOrderValue);

        assertEquals(redisFixture, redisOrderValue);

        redisTemplate.delete(orderTokenCookieValue);
    }

    @Test
    @DisplayName(value = "회원이 상품 상세 페이지에서 결제 요청 시 상품 결제 정보 조회. 잘못된 싱픔 옵션 아이디를 전달한 경우")
    void orderProductByMemberWrongOptionId() throws Exception {
        Product fixture = productList.get(0);
        // 정상 fixture
        List<OrderProductRequestDTO> orderProductDTO = new ArrayList<>(
                fixture.getProductOptions()
                .stream()
                .map(v -> new OrderProductRequestDTO(v.getId(), 3))
                .toList()
        );

        // 존재하지 않는 옵션 아이디 fixture
        long wrongOptionId = productOptionList.get(productOptionList.size() - 1).getId() + 10;
        orderProductDTO.add(
                new OrderProductRequestDTO(
                        wrongOptionId,
                        2
                )
        );

        String requestDTO = om.writeValueAsString(orderProductDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Validation 관련 Exception과 다르게 조회시 데이터가 없음을 의미해야 하기 때문에
        // IllegalArgumentException 발생이 검증되어야 함.
        Exception ex = result.getResolvedException();
        assertInstanceOf(IllegalArgumentException.class, ex);

        String content = result.getResponse().getContentAsString();
        Map<String, String> cookieMap = tokenFixture.getCookieMap(result);
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());

        assertTrue(cookieMap.isEmpty());
    }

    @Test
    @DisplayName(value = "회원이 상품 상세 페이지에서 결제 요청 시 상품 결제 정보 조회. 옵션 아이디값이 1보다 작은 경우")
    void orderProductByMemberValidationOptionIdLT1() throws Exception {
        Product fixture = productList.get(0);
        // 정상 fixture
        List<OrderProductRequestDTO> orderProductDTO = new ArrayList<>(
                fixture.getProductOptions()
                        .stream()
                        .map(v -> new OrderProductRequestDTO(v.getId(), 3))
                        .toList()
        );

        // optionId == 0
        orderProductDTO.add(
                new OrderProductRequestDTO(
                        0L,
                        2
                )
        );

        String requestDTO = om.writeValueAsString(orderProductDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        Map<String, String> cookieMap = tokenFixture.getCookieMap(result);
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertTrue(cookieMap.isEmpty());
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("optionId", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원이 상품 상세 페이지에서 결제 요청 시 상품 결제 정보 조회. 상품 수량이 1보다 작은 경우")
    void orderProductByMemberValidationCountLT1() throws Exception {
        Product fixture = productList.get(0);
        // 정상 fixture
        List<OrderProductRequestDTO> orderProductDTO = new ArrayList<>(
                fixture.getProductOptions()
                        .stream()
                        .map(v -> new OrderProductRequestDTO(v.getId(), 3))
                        .toList()
        );

        // optionId == 0
        orderProductDTO.add(
                new OrderProductRequestDTO(
                        10L,
                        0
                )
        );

        String requestDTO = om.writeValueAsString(orderProductDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "product")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        Map<String, String> cookieMap = tokenFixture.getCookieMap(result);
        ValidationExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertTrue(cookieMap.isEmpty());
        assertEquals(BAD_REQUEST_ERROR_CODE.getHttpStatus().value(), response.errorCode());
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertFalse(response.errors().isEmpty());

        assertEquals(1, response.errors().size());

        ValidationError responseObj = response.errors().get(0);

        assertEquals("count", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원이 장바구니 페이지에서 결제 요청 시 상품 결제 정보 조회.")
    void orderCartByMember() throws Exception {
        List<Long> requestDetailIds = new ArrayList<>();
        List<OrderDataDTO> responseFixture = new ArrayList<>();
        List<OrderItemVO> redisFixtureFieldList = new ArrayList<>();
        int totalPrice = 0;

        for(CartDetail detail : memberCart.getCartDetailList()) {
            requestDetailIds.add(detail.getId());

            for(ProductOption option : productOptionList) {
                long detailOptionId = detail.getProductOption().getId();

                if(detailOptionId == option.getId()) {
                    Product productFixture = option.getProduct();
                    int price = (int) (productFixture.getProductPrice() * (1 - ((double) productFixture.getProductDiscount() / 100))) * detail.getCartCount();
                    totalPrice += price;
                    responseFixture.add(
                            new OrderDataDTO(
                                    productFixture.getId(),
                                    option.getId(),
                                    productFixture.getProductName(),
                                    option.getSize(),
                                    option.getColor(),
                                    detail.getCartCount(),
                                    price
                            )
                    );

                    redisFixtureFieldList.add(
                            new OrderItemVO(
                                    productFixture.getId(),
                                    option.getId(),
                                    detail.getCartCount(),
                                    price
                            )
                    );

                    break;
                }
            }
        }

        PreOrderDataVO redisFixture = new PreOrderDataVO(member.getUserId(), redisFixtureFieldList, totalPrice);
        String requestDTO = om.writeValueAsString(requestDetailIds);
        MvcResult result = mockMvc.perform(post(URL_PREFIX + "cart")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        Map<String, String> cookieMap = tokenFixture.getCookieMap(result);
        OrderDataResponseDTO response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(totalPrice, response.totalPrice());
        assertEquals(responseFixture.size(), response.orderData().size());

        response.orderData().forEach(v -> assertTrue(responseFixture.contains(v)));
        assertNotNull(cookieMap);
        assertEquals(1, cookieMap.size());
        String orderTokenCookieValue = cookieMap.getOrDefault("order", null);
        assertNotNull(orderTokenCookieValue);

        PreOrderDataVO redisOrderValue = orderRedisTemplate.opsForValue().get(orderTokenCookieValue);
        assertNotNull(redisOrderValue);

        assertEquals(redisFixture.userId(), redisOrderValue.userId());
        assertEquals(redisFixture.totalPrice(), redisOrderValue.totalPrice());
        assertEquals(redisFixture.orderData().size(), redisOrderValue.orderData().size());
        redisOrderValue.orderData().forEach(v -> assertTrue(redisFixture.orderData().contains(v)));

        redisTemplate.delete(orderTokenCookieValue);
    }

    @Test
    @DisplayName(value = "회원이 장바구니 페이지에서 결제 요청 시 상품 결제 정보 조회. 잘못된 장바구니 아이디 전달로 인해 데이터가 없는 경우")
    void orderCartByMemberWrongIds() throws Exception {
        Long maxDetailId = Stream.of(memberCart, noneMemberCart, anonymousCart)
                                .flatMap(v -> v.getCartDetailList().stream())
                                .map(CartDetail::getId)
                                .max(Long::compareTo)
                                .get() + 1L;

        List<Long> requestDetailIds = List.of(maxDetailId);

        String requestDTO = om.writeValueAsString(requestDetailIds);
        MvcResult result = mockMvc.perform(post(URL_PREFIX + "cart")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();

        // 다른 사람의 장바구니 상세 아이디가 아니고
        // 아예 존재하지 않는 상세 아이디로 요청하는 경우
        // CustomNotFoundException이 발생해야 함.
        // 다른 사람의 상세 아이디인 경우 CustomAccessDenied,
        // 유효성 검사에 실패한 경우 IllegalArgumentException이 발생하므로 ResolvedException을 통해 검증 필요.
        Exception ex = result.getResolvedException();
        assertInstanceOf(CustomNotFoundException.class, ex);

        String content = result.getResponse().getContentAsString();
        Map<String, String> cookieMap = tokenFixture.getCookieMap(result);
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertTrue(cookieMap.isEmpty());
    }

    @Test
    @DisplayName(value = "회원이 장바구니 페이지에서 결제 요청 시 상품 결제 정보 조회. 조회한 데이터와 사용자가 일치하지 않는 경우")
    void orderCartByMemberWrongMember() throws Exception {
        List<Long> requestDetailIds = noneMemberCart.getCartDetailList()
                .stream()
                .mapToLong(CartDetail::getId)
                .boxed()
                .toList();

        String requestDTO = om.writeValueAsString(requestDetailIds);
        MvcResult result = mockMvc.perform(post(URL_PREFIX + "cart")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isForbidden())
                .andReturn();

        // 다른 사람의 장바구니 상세 아이디가 아니고
        // 아예 존재하지 않는 상세 아이디로 요청하는 경우
        // CustomNotFoundException이 발생해야 함.
        // 다른 사람의 상세 아이디인 경우 CustomAccessDenied,
        // 유효성 검사에 실패한 경우 IllegalArgumentException이 발생하므로 ResolvedException을 통해 검증 필요.
        Exception ex = result.getResolvedException();
        assertInstanceOf(CustomAccessDeniedException.class, ex);

        String content = result.getResponse().getContentAsString();
        Map<String, String> cookieMap = tokenFixture.getCookieMap(result);
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.FORBIDDEN.getMessage(), response.errorMessage());
        assertTrue(cookieMap.isEmpty());
    }

    @Test
    @DisplayName(value = "회원이 장바구니 페이지에서 결제 요청 시 상품 결제 정보 조회. 장바구니 아이디가 1보다 작은 경우")
    void orderCartByMemberValidationDetailIdLT1() throws Exception {
        List<Long> requestDetailIds = List.of(0L);

        String requestDTO = om.writeValueAsString(requestDetailIds);
        MvcResult result = mockMvc.perform(post(URL_PREFIX + "cart")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();

        // 다른 사람의 장바구니 상세 아이디가 아니고
        // 아예 존재하지 않는 상세 아이디로 요청하는 경우
        // CustomNotFoundException이 발생해야 함.
        // 다른 사람의 상세 아이디인 경우 CustomAccessDenied,
        // 유효성 검사에 실패한 경우 IllegalArgumentException이 발생하므로 ResolvedException을 통해 검증 필요.
        Exception ex = result.getResolvedException();
        assertInstanceOf(IllegalArgumentException.class, ex);

        String content = result.getResponse().getContentAsString();
        Map<String, String> cookieMap = tokenFixture.getCookieMap(result);
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertTrue(cookieMap.isEmpty());
    }

    @Test
    @DisplayName(value = "회원이 장바구니 페이지에서 결제 요청 시 상품 결제 정보 조회. 요청 리스트가 비어있는 경우")
    void orderCartByMemberValidationEmptyList() throws Exception {
        List<Long> requestDetailIds = List.of();

        String requestDTO = om.writeValueAsString(requestDetailIds);
        MvcResult result = mockMvc.perform(post(URL_PREFIX + "cart")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();

        // 다른 사람의 장바구니 상세 아이디가 아니고
        // 아예 존재하지 않는 상세 아이디로 요청하는 경우
        // CustomNotFoundException이 발생해야 함.
        // 다른 사람의 상세 아이디인 경우 CustomAccessDenied,
        // 유효성 검사에 실패한 경우 IllegalArgumentException이 발생하므로 ResolvedException을 통해 검증 필요.
        Exception ex = result.getResolvedException();
        assertInstanceOf(IllegalArgumentException.class, ex);

        String content = result.getResponse().getContentAsString();
        Map<String, String> cookieMap = tokenFixture.getCookieMap(result);
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(BAD_REQUEST_ERROR_CODE.getMessage(), response.errorMessage());
        assertTrue(cookieMap.isEmpty());
    }

    @Test
    @DisplayName(value = "비회원이 장바구니 페이지에서 결제 요청 시 상품 결제 정보 조회. 조회한 데이터와 CookieId가 일치하지 않는 경우")
    void orderCartByAnonymousCookieIdNotEquals() throws Exception {
        List<Long> requestDetailIds = anonymousCart.getCartDetailList()
                .stream()
                .mapToLong(CartDetail::getId)
                .boxed()
                .toList();

        String requestDTO = om.writeValueAsString(requestDetailIds);
        MvcResult result = mockMvc.perform(post(URL_PREFIX + "cart")
                        .cookie(new Cookie(cookieProperties.getCart().getHeader(), "noneAnonymousCookieValue"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isForbidden())
                .andReturn();

        // cartCookie가 포함된 요청이기 때문에 데이터 조회해서의 불일치로 CustomAccessDenied가 발생
        Exception ex = result.getResolvedException();
        assertInstanceOf(CustomAccessDeniedException.class, ex);

        String content = result.getResponse().getContentAsString();
        Map<String, String> cookieMap = tokenFixture.getCookieMap(result);
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.FORBIDDEN.getMessage(), response.errorMessage());
        assertTrue(cookieMap.isEmpty());
    }

    @Test
    @DisplayName(value = "비회원이 장바구니 페이지에서 결제 요청 시 상품 결제 정보 조회.")
    void orderCartByAnonymous() throws Exception {
        List<Long> requestDetailIds = new ArrayList<>();
        List<OrderDataDTO> responseFixture = new ArrayList<>();
        List<OrderItemVO> redisFixtureFieldList = new ArrayList<>();
        int totalPrice = 0;

        for(CartDetail detail : anonymousCart.getCartDetailList()) {
            requestDetailIds.add(detail.getId());

            for(ProductOption option : productOptionList) {
                long detailOptionId = detail.getProductOption().getId();

                if(detailOptionId == option.getId()) {
                    Product productFixture = option.getProduct();
                    int price = (int) (productFixture.getProductPrice() * (1 - ((double) productFixture.getProductDiscount() / 100))) * detail.getCartCount();
                    totalPrice += price;
                    responseFixture.add(
                            new OrderDataDTO(
                                    productFixture.getId(),
                                    option.getId(),
                                    productFixture.getProductName(),
                                    option.getSize(),
                                    option.getColor(),
                                    detail.getCartCount(),
                                    price
                            )
                    );

                    redisFixtureFieldList.add(
                            new OrderItemVO(
                                    productFixture.getId(),
                                    option.getId(),
                                    detail.getCartCount(),
                                    price
                            )
                    );

                    break;
                }
            }
        }

        PreOrderDataVO redisFixture = new PreOrderDataVO(anonymous.getUserId(), redisFixtureFieldList, totalPrice);
        String requestDTO = om.writeValueAsString(requestDetailIds);
        MvcResult result = mockMvc.perform(post(URL_PREFIX + "cart")
                        .cookie(new Cookie(cookieProperties.getCart().getHeader(), ANONYMOUS_CART_COOKIE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        Map<String, String> cookieMap = tokenFixture.getCookieMap(result);
        OrderDataResponseDTO response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(totalPrice, response.totalPrice());
        assertEquals(responseFixture.size(), response.orderData().size());

        response.orderData().forEach(v -> assertTrue(responseFixture.contains(v)));
        assertNotNull(cookieMap);
        assertEquals(1, cookieMap.size());
        String orderTokenCookieValue = cookieMap.getOrDefault("order", null);
        assertNotNull(orderTokenCookieValue);

        PreOrderDataVO redisOrderValue = orderRedisTemplate.opsForValue().get(orderTokenCookieValue);
        assertNotNull(redisOrderValue);

        assertEquals(redisFixture.userId(), redisOrderValue.userId());
        assertEquals(redisFixture.totalPrice(), redisOrderValue.totalPrice());
        assertEquals(redisFixture.orderData().size(), redisOrderValue.orderData().size());
        redisOrderValue.orderData().forEach(v -> assertTrue(redisFixture.orderData().contains(v)));

        redisTemplate.delete(orderTokenCookieValue);
    }

    @Test
    @DisplayName(value = "비회원이 장바구니 페이지에서 결제 요청 시 상품 결제 정보 조회. cookieId가 없는 경우")
    void orderCartByAnonymousNotExistCookieId() throws Exception {
        List<Long> requestDetailIds = anonymousCart.getCartDetailList()
                .stream()
                .mapToLong(CartDetail::getId)
                .boxed()
                .toList();

        String requestDTO = om.writeValueAsString(requestDetailIds);
        MvcResult result = mockMvc.perform(post(URL_PREFIX + "cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isBadRequest())
                .andReturn();

        // 비회원인데 쿠키가 없으므로 Controller에서 검증 체크로 인해 CustomNotFound 발생
        Exception ex = result.getResolvedException();
        assertInstanceOf(CustomNotFoundException.class, ex);

        String content = result.getResponse().getContentAsString();
        Map<String, String> cookieMap = tokenFixture.getCookieMap(result);
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.BAD_REQUEST.getMessage(), response.errorMessage());
        assertTrue(cookieMap.isEmpty());
    }

    @Test
    @DisplayName(value = "회원의 결제 API 호출 직전 주문 데이터 검증")
    void validateOrderByMember() throws Exception {
        Product productFixture = productList.get(0);
        List<OrderItemVO> fixtureOrderDataList = new ArrayList<>();
        List<OrderDataDTO> requestOrderDataDTOList = new ArrayList<>();
        int totalPrice = 0;
        for(ProductOption option : productFixture.getProductOptions()) {
            int orderCount = 3;
            int price = (int) (productFixture.getProductPrice() * (1 - ((double) productFixture.getProductDiscount() / 100))) * orderCount;
            totalPrice += price;

            fixtureOrderDataList.add(
                    new OrderItemVO(
                            productFixture.getId(),
                            option.getId(),
                            orderCount,
                            price
                    )
            );

            requestOrderDataDTOList.add(
                    new OrderDataDTO(
                            productFixture.getId(),
                            option.getId(),
                            productFixture.getProductName(),
                            option.getSize(),
                            option.getColor(),
                            orderCount,
                            price
                    )
            );
        }

        PreOrderDataVO redisDataFixture = new PreOrderDataVO(member.getUserId(), fixtureOrderDataList, totalPrice);
        OrderDataResponseDTO requestOrderDataDTO = new OrderDataResponseDTO(requestOrderDataDTOList, totalPrice);

        orderRedisTemplate.opsForValue().set(ORDER_TOKEN, redisDataFixture);

        String requestDTO = om.writeValueAsString(requestOrderDataDTO);

        mockMvc.perform(post(URL_PREFIX + "validate")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Test
    @DisplayName(value = "회원의 결제 API 호출 직전 주문 데이터 검증. orderData 리스트가 비어있는 경우")
    void validateOrderByMemberValidationOrderDataListIsEmpty() throws Exception {
        List<OrderDataDTO> requestOrderDataDTOList = new ArrayList<>();
        OrderDataResponseDTO requestOrderDataDTO = new OrderDataResponseDTO(requestOrderDataDTOList, 1000);

        String requestDTO = om.writeValueAsString(requestOrderDataDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "validate")
                                    .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                    .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                    .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                                    .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
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

        assertEquals("orderData", responseObj.field());
        assertEquals("NotEmpty", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원의 결제 API 호출 직전 주문 데이터 검증. 총 금액이 음수인 경우")
    void validateOrderByMemberValidationTotalPriceIsNegativeNumber() throws Exception {
        Product productFixture = productList.get(0);
        List<OrderDataDTO> requestOrderDataDTOList = new ArrayList<>();
        int totalPrice = -1;
        for(ProductOption option : productFixture.getProductOptions()) {
            int orderCount = 3;
            int price = (int) (productFixture.getProductPrice() * (1 - ((double) productFixture.getProductDiscount() / 100))) * orderCount;

            requestOrderDataDTOList.add(
                    new OrderDataDTO(
                            productFixture.getId(),
                            option.getId(),
                            productFixture.getProductName(),
                            option.getSize(),
                            option.getColor(),
                            orderCount,
                            price
                    )
            );
        }

        OrderDataResponseDTO requestOrderDataDTO = new OrderDataResponseDTO(requestOrderDataDTOList, totalPrice);

        String requestDTO = om.writeValueAsString(requestOrderDataDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "validate")
                                .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                                .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                                .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                                .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
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

        assertEquals("totalPrice", responseObj.field());
        assertEquals("Min", responseObj.constraint());
    }

    @Test
    @DisplayName(value = "회원의 결제 API 호출 직전 주문 데이터 검증. orderData 리스트가 비어있고 총 금액이 음수인 경우")
    void validateOrderByMemberValidationAllParameterIsWrong() throws Exception {
        List<OrderDataDTO> requestOrderDataDTOList = new ArrayList<>();
        OrderDataResponseDTO requestOrderDataDTO = new OrderDataResponseDTO(requestOrderDataDTOList, -1);

        String requestDTO = om.writeValueAsString(requestOrderDataDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "validate")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
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
        validationMap.put("orderData", "NotEmpty");
        validationMap.put("totalPrice", "Min");

        response.errors().forEach(v -> {
            String constraint = validationMap.getOrDefault(v.field(), null);

            assertNotNull(constraint);
            assertEquals(constraint, v.constraint());
        });
    }

    @Test
    @DisplayName(value = "비회원의 결제 API 호출 직전 주문 데이터 검증")
    void validateOrderByAnonymous() throws Exception {
        Product productFixture = productList.get(0);
        List<OrderItemVO> fixtureOrderDataList = new ArrayList<>();
        List<OrderDataDTO> requestOrderDataDTOList = new ArrayList<>();
        int totalPrice = 0;
        for(ProductOption option : productFixture.getProductOptions()) {
            int orderCount = 3;
            int price = (int) (productFixture.getProductPrice() * (1 - ((double) productFixture.getProductDiscount() / 100))) * orderCount;
            totalPrice += price;

            fixtureOrderDataList.add(
                    new OrderItemVO(
                            productFixture.getId(),
                            option.getId(),
                            orderCount,
                            price
                    )
            );

            requestOrderDataDTOList.add(
                    new OrderDataDTO(
                            productFixture.getId(),
                            option.getId(),
                            productFixture.getProductName(),
                            option.getSize(),
                            option.getColor(),
                            orderCount,
                            price
                    )
            );
        }

        PreOrderDataVO redisDataFixture = new PreOrderDataVO(anonymous.getUserId(), fixtureOrderDataList, totalPrice);
        OrderDataResponseDTO requestOrderDataDTO = new OrderDataResponseDTO(requestOrderDataDTOList, totalPrice);

        orderRedisTemplate.opsForValue().set(ORDER_TOKEN, redisDataFixture);

        String requestDTO = om.writeValueAsString(requestOrderDataDTO);

        mockMvc.perform(post(URL_PREFIX + "validate")
                        .cookie(new Cookie(cookieProperties.getCart().getHeader(), ANONYMOUS_CART_COOKIE))
                        .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Test
    @DisplayName(value = "결제 API 호출 직전 주문 데이터 검증. orderToken이 없는 경우")
    void validateOrderNotExistsOrderToken() throws Exception {
        Product productFixture = productList.get(0);
        List<OrderDataDTO> requestOrderDataDTOList = new ArrayList<>();
        int totalPrice = 0;
        for(ProductOption option : productFixture.getProductOptions()) {
            int orderCount = 3;
            int price = (int) (productFixture.getProductPrice() * (1 - ((double) productFixture.getProductDiscount() / 100))) * orderCount;
            totalPrice += price;

            requestOrderDataDTOList.add(
                    new OrderDataDTO(
                            productFixture.getId(),
                            option.getId(),
                            productFixture.getProductName(),
                            option.getSize(),
                            option.getColor(),
                            orderCount,
                            price
                    )
            );
        }
        OrderDataResponseDTO requestOrderDataDTO = new OrderDataResponseDTO(requestOrderDataDTOList, totalPrice);
        String requestDTO = om.writeValueAsString(requestOrderDataDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "validate")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isUnauthorized())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.ORDER_SESSION_EXPIRED.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "결제 API 호출 직전 주문 데이터 검증. redis에 저장된 데이터가 없는 경우")
    void validateOrderNotFoundRedisCachingData() throws Exception {
        Product productFixture = productList.get(0);
        List<OrderDataDTO> requestOrderDataDTOList = new ArrayList<>();
        int totalPrice = 0;
        for(ProductOption option : productFixture.getProductOptions()) {
            int orderCount = 3;
            int price = (int) (productFixture.getProductPrice() * (1 - ((double) productFixture.getProductDiscount() / 100))) * orderCount;
            totalPrice += price;

            requestOrderDataDTOList.add(
                    new OrderDataDTO(
                            productFixture.getId(),
                            option.getId(),
                            productFixture.getProductName(),
                            option.getSize(),
                            option.getColor(),
                            orderCount,
                            price
                    )
            );
        }
        OrderDataResponseDTO requestOrderDataDTO = new OrderDataResponseDTO(requestOrderDataDTOList, totalPrice);

        String requestDTO = om.writeValueAsString(requestOrderDataDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "validate")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isUnauthorized())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.ORDER_SESSION_EXPIRED.getMessage(), response.errorMessage());
    }

    @Test
    @DisplayName(value = "결제 API 호출 직전 주문 데이터 검증. Redis 데이터와 요청 데이터가 일치하지 않는 경우")
    void validateOrderNotEqualsData() throws Exception {
        Product productFixture = productList.get(0);
        List<OrderItemVO> fixtureOrderDataList = new ArrayList<>();
        List<OrderDataDTO> requestOrderDataDTOList = new ArrayList<>();
        int totalPrice = 0;
        for(ProductOption option : productFixture.getProductOptions()) {
            int orderCount = 3;
            int price = (int) (productFixture.getProductPrice() * (1 - ((double) productFixture.getProductDiscount() / 100))) * orderCount;
            totalPrice += price;

            fixtureOrderDataList.add(
                    new OrderItemVO(
                            productFixture.getId(),
                            option.getId(),
                            orderCount - 1,
                            price - 1
                    )
            );

            requestOrderDataDTOList.add(
                    new OrderDataDTO(
                            productFixture.getId(),
                            option.getId(),
                            productFixture.getProductName(),
                            option.getSize(),
                            option.getColor(),
                            orderCount,
                            price
                    )
            );
        }

        PreOrderDataVO redisDataFixture = new PreOrderDataVO(member.getUserId(), fixtureOrderDataList, totalPrice);
        OrderDataResponseDTO requestOrderDataDTO = new OrderDataResponseDTO(requestOrderDataDTOList, totalPrice);

        orderRedisTemplate.opsForValue().set(ORDER_TOKEN, redisDataFixture);

        String requestDTO = om.writeValueAsString(requestOrderDataDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "validate")
                        .header(tokenProperties.getAccess().getHeader(), accessTokenValue)
                        .cookie(new Cookie(tokenProperties.getRefresh().getHeader(), refreshTokenValue))
                        .cookie(new Cookie(cookieProperties.getIno().getHeader(), inoValue))
                        .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isUnauthorized())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.ORDER_SESSION_EXPIRED.getMessage(), response.errorMessage());
    }
}
