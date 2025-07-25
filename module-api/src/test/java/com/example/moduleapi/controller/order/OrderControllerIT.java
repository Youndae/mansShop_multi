package com.example.moduleapi.controller.order;

import com.example.moduleapi.ModuleApiApplication;
import com.example.moduleapi.config.exception.ExceptionEntity;
import com.example.moduleapi.fixture.TokenFixture;
import com.example.moduleauth.repository.AuthRepository;
import com.example.moduleauth.repository.MemberRepository;
import com.example.modulecart.repository.CartRepository;
import com.example.modulecommon.fixture.CartFixture;
import com.example.modulecommon.fixture.ClassificationFixture;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.dto.response.ResponseMessageDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.ErrorCode;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleorder.model.dto.business.OrderDataDTO;
import com.example.moduleorder.model.dto.in.OrderProductRequestDTO;
import com.example.moduleorder.model.dto.out.OrderDataResponseDTO;
import com.example.moduleorder.model.vo.OrderItemVO;
import com.example.moduleorder.model.vo.PreOrderDataVO;
import com.example.moduleorder.repository.ProductOrderDetailRepository;
import com.example.moduleorder.repository.ProductOrderRepository;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    @Autowired
    private EntityManager em;

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

        em.flush();
        em.clear();
    }

    @AfterEach
    void cleanUP() {
        String accessKey = tokenMap.get("accessKey");
        String refreshKey = tokenMap.get("refreshKey");

        redisTemplate.delete(accessKey);
        redisTemplate.delete(refreshKey);
        orderRedisTemplate.delete(ORDER_TOKEN);
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
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
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
        List<OrderProductRequestDTO> orderProductDTO = new ArrayList<>();

        for(int i = 0; i < fixture.getProductOptions().size(); i++) {
            ProductOption option = fixture.getProductOptions().get(i);
            orderProductDTO.add(
                    new OrderProductRequestDTO(
                            i == 0 ? 0L : option.getId(),
                            3
                    )
            );
        }

        String requestDTO = om.writeValueAsString(orderProductDTO);

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "product")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().is(400))
                .andReturn();
        String content = result.getResponse().getContentAsString();
        Map<String, String> cookieMap = tokenFixture.getCookieMap(result);
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.NOT_FOUND.getMessage(), response.errorMessage());

        assertTrue(cookieMap.isEmpty());
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
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
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
    @DisplayName(value = "회원이 장바구니 페이지에서 결제 요청 시 상품 결제 정보 조회. 잘못된 장바구니 아이디 전달로 인해 데이터가 없는 경우")
    void orderCartByMemberWrongIds() throws Exception {
        List<Long> requestDetailIds = List.of(0L);

        String requestDTO = om.writeValueAsString(requestDetailIds);
        MvcResult result = mockMvc.perform(post(URL_PREFIX + "cart")
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().is(400))
                .andReturn();
        String content = result.getResponse().getContentAsString();
        Map<String, String> cookieMap = tokenFixture.getCookieMap(result);
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.NOT_FOUND.getMessage(), response.errorMessage());
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
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().is(403))
                .andReturn();
        String content = result.getResponse().getContentAsString();
        Map<String, String> cookieMap = tokenFixture.getCookieMap(result);
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.ACCESS_DENIED.getMessage(), response.errorMessage());
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
                        .cookie(new Cookie(cartCookieHeader, "noneAnonymousCookieValue"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().is(403))
                .andReturn();
        String content = result.getResponse().getContentAsString();
        Map<String, String> cookieMap = tokenFixture.getCookieMap(result);
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.ACCESS_DENIED.getMessage(), response.errorMessage());
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
                        .cookie(new Cookie(cartCookieHeader, ANONYMOUS_CART_COOKIE))
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
                .andExpect(status().is(403))
                .andReturn();
        String content = result.getResponse().getContentAsString();
        Map<String, String> cookieMap = tokenFixture.getCookieMap(result);
        ExceptionEntity response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(ErrorCode.ACCESS_DENIED.getMessage(), response.errorMessage());
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

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "validate")
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
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(Result.OK.getResultKey(), response.message());
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

        MvcResult result = mockMvc.perform(post(URL_PREFIX + "validate")
                        .cookie(new Cookie(cartCookieHeader, ANONYMOUS_CART_COOKIE))
                        .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        ResponseMessageDTO response = om.readValue(
                content,
                new TypeReference<>(){}
        );

        assertNotNull(response);
        assertEquals(Result.OK.getResultKey(), response.message());
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
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().is(440))
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
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
                        .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().is(440))
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
                        .header(accessHeader, accessTokenValue)
                        .cookie(new Cookie(refreshHeader, refreshTokenValue))
                        .cookie(new Cookie(inoHeader, inoValue))
                        .cookie(new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestDTO))
                .andExpect(status().is(440))
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
