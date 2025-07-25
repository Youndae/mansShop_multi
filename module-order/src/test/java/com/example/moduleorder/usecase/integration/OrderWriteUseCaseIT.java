package com.example.moduleorder.usecase.integration;


import com.example.moduleauth.repository.MemberRepository;
import com.example.modulecart.repository.CartDetailRepository;
import com.example.modulecart.repository.CartRepository;
import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.customException.CustomNotFoundException;
import com.example.modulecommon.customException.CustomOrderSessionExpiredException;
import com.example.modulecommon.fixture.CartFixture;
import com.example.modulecommon.fixture.ClassificationFixture;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleorder.ModuleOrderApplication;
import com.example.moduleorder.model.dto.business.OrderDataDTO;
import com.example.moduleorder.model.dto.in.OrderProductRequestDTO;
import com.example.moduleorder.model.dto.out.OrderDataResponseDTO;
import com.example.moduleorder.model.vo.OrderItemVO;
import com.example.moduleorder.model.vo.PreOrderDataVO;
import com.example.moduleorder.usecase.OrderWriteUseCase;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ModuleOrderApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@Transactional
public class OrderWriteUseCaseIT {

    /**
     * 주문 데이터 처리 통합 테스트는 module-test 에서 따로 처리.
     * PeriodSalesSummaryRepository와 ProductSalesSummaryRepository 사용에 있어서
     * module-order가 module-admin을 참조하면 양방향 참조가 되기 때문.
     */

    @Autowired
    private OrderWriteUseCase orderWriteUseCase;

    @Autowired
    private MemberRepository memberRepository;

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
    private RedisTemplate<String, PreOrderDataVO> orderRedisTemplate;

    @Autowired
    private EntityManager em;

    @Value("#{jwt['cookie.cart.header']}")
    private String cartCookieHeader;

    private Member member;

    private Member anonymous;

    private Product product;

    private List<ProductOption> productOptionList;

    private Cart memberCart;

    private List<CartDetail> memberCartDetailList;

    private Cart anonymousCart;

    private static final String ANONYMOUS_COOKIE_VALUE = "testCookieValue";

    private static final String ORDER_TOKEN = "testOrderTokenValue";

    @BeforeEach
    void init() {
        MemberAndAuthFixtureDTO memberAndAuthFixtureDTO = MemberAndAuthFixture.createDefaultMember(1);
        MemberAndAuthFixtureDTO anonymousFixtureDTO = MemberAndAuthFixture.createAnonymous();
        List<Member> memberList = new ArrayList<>(memberAndAuthFixtureDTO.memberList());
        memberList.addAll(anonymousFixtureDTO.memberList());
        memberRepository.saveAll(memberList);
        member = memberAndAuthFixtureDTO.memberList().get(0);
        anonymous = anonymousFixtureDTO.memberList().get(0);

        List<Classification> classificationList = ClassificationFixture.createClassifications();
        classificationRepository.saveAll(classificationList);

        product = ProductFixture.createProductFixtureList(1, classificationList.get(0)).get(0);
        productOptionList = product.getProductOptions();
        productRepository.save(product);
        productOptionRepository.saveAll(productOptionList);

        memberCart = CartFixture.createDefaultMemberCart(List.of(member), productOptionList).get(0);
        anonymousCart = CartFixture.createSaveAnonymousCartMultipleOptions(productOptionList, anonymous, ANONYMOUS_COOKIE_VALUE);

        List<Cart> saveCartList = List.of(memberCart, anonymousCart);
        cartRepository.saveAll(saveCartList);
        List<CartDetail> saveCartDetailList = saveCartList.stream().flatMap(v -> v.getCartDetailList().stream()).toList();
        cartDetailRepository.saveAll(saveCartDetailList);

        memberCartDetailList = memberCart.getCartDetailList();

        em.flush();
        em.clear();
    }

    @AfterEach
    void cleanUp() {
        cartDetailRepository.deleteAll();
        cartRepository.deleteAll();
        memberRepository.deleteAll();
        productOptionRepository.deleteAll();
        productRepository.deleteAll();
        classificationRepository.deleteAll();

        orderRedisTemplate.delete(ORDER_TOKEN);
    }

    @Test
    @DisplayName(value = "결제 요청 시 상품 정보 조회")
    void getProductOrderData() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        List<OrderProductRequestDTO> optionIdAndCountDTO = new ArrayList<>();
        List<OrderDataDTO> orderDataDTOList = new ArrayList<>();
        List<OrderItemVO> orderItemVOList = new ArrayList<>();
        int totalPrice = 0;
        for(int i = 0; i < productOptionList.size(); i++){
            ProductOption option = productOptionList.get(i);
            int count = (int) (option.getId() * (i + 1));
            int price = product.getProductPrice() * count;
            totalPrice += price;

            optionIdAndCountDTO.add(
                    new OrderProductRequestDTO(option.getId(), count)
            );

            orderDataDTOList.add(
                    new OrderDataDTO(
                            product.getId(),
                            option.getId(),
                            product.getProductName(),
                            option.getSize(),
                            option.getColor(),
                            count,
                            price
                    )
            );

            orderItemVOList.add(
                    new OrderItemVO(
                            product.getId(),
                            option.getId(),
                            count,
                            price
                    )
            );
        }
        OrderDataResponseDTO result = assertDoesNotThrow(() -> orderWriteUseCase.getProductOrderData(optionIdAndCountDTO, null, response, member.getUserId()));

        assertNotNull(result);
        assertFalse(result.orderData().isEmpty());
        assertEquals(orderDataDTOList.size(), result.orderData().size());
        assertEquals(totalPrice, result.totalPrice());

        orderDataDTOList.forEach(v -> assertTrue(result.orderData().contains(v)));

        String orderToken = response.getHeader("Set-Cookie").split(";", 2)[0].split("=", 2)[1];
        assertNotNull(orderToken);
        assertNotEquals("", orderToken);

        PreOrderDataVO preOrderDataVO = orderRedisTemplate.opsForValue().get(orderToken);
        assertNotNull(preOrderDataVO);
        assertEquals(member.getUserId(), preOrderDataVO.userId());
        assertEquals(totalPrice, preOrderDataVO.totalPrice());
        orderItemVOList.forEach(v -> assertTrue(preOrderDataVO.orderData().contains(v)));

        orderRedisTemplate.delete(orderToken);
    }

    @Test
    @DisplayName(value = "결제 요청 시 상품 정보 조회. 상품 옵션 아이디들이 잘못 된 경우")
    void getProductOrderDataWrongIds() {
        List<OrderProductRequestDTO> optionIdAndCountDTO = List.of(new OrderProductRequestDTO(0L, 3));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThrows(IllegalArgumentException.class, () -> orderWriteUseCase.getProductOrderData(optionIdAndCountDTO, null, response, member.getUserId()));
    }

    @Test
    @DisplayName(value = "결제 요청 시 장바구니 상품 정보 조회")
    void getCartOrderData() {
        List<Long> cartDetailIds = new ArrayList<>();
        List<OrderDataDTO> orderDataDTOList = new ArrayList<>();
        List<OrderItemVO> orderItemVOList = new ArrayList<>();
        int totalPrice = 0;
        for(int i = 0; i < memberCartDetailList.size(); i++) {
            CartDetail detail = memberCartDetailList.get(i);
            cartDetailIds.add(detail.getId());
            int price = product.getProductPrice() * detail.getCartCount();
            totalPrice += price;
            orderDataDTOList.add(
                    new OrderDataDTO(
                            product.getId(),
                            detail.getProductOption().getId(),
                            product.getProductName(),
                            detail.getProductOption().getSize(),
                            detail.getProductOption().getColor(),
                            detail.getCartCount(),
                            price
                    )
            );

            orderItemVOList.add(
                    new OrderItemVO(
                            product.getId(),
                            detail.getProductOption().getId(),
                            detail.getCartCount(),
                            price
                    )
            );
        }

        MockHttpServletResponse response = new MockHttpServletResponse();

        OrderDataResponseDTO result = assertDoesNotThrow(() -> orderWriteUseCase.getCartOrderData(cartDetailIds, null, null, member.getUserId(), response));

        assertNotNull(result);
        assertFalse(result.orderData().isEmpty());
        assertEquals(orderDataDTOList.size(), result.orderData().size());
        assertEquals(totalPrice, result.totalPrice());

        orderDataDTOList.forEach(v -> assertTrue(result.orderData().contains(v)));

        String orderToken = response.getHeader("Set-Cookie").split(";", 2)[0].split("=", 2)[1];
        assertNotNull(orderToken);
        assertNotEquals("", orderToken);

        PreOrderDataVO preOrderDataVO = orderRedisTemplate.opsForValue().get(orderToken);
        assertNotNull(preOrderDataVO);
        assertEquals(member.getUserId(), preOrderDataVO.userId());
        assertEquals(totalPrice, preOrderDataVO.totalPrice());
        orderItemVOList.forEach(v -> assertTrue(preOrderDataVO.orderData().contains(v)));

        orderRedisTemplate.delete(orderToken);
    }

    @Test
    @DisplayName(value = "결제 요청 시 장바구니 상품 정보 조회. 장바구니 상세 아이디들이 잘못 된 경우")
    void getCartOrderDataWrongIds() {
        List<Long> cartDetailIds = List.of(0L);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThrows(
                CustomNotFoundException.class,
                () -> orderWriteUseCase.getCartOrderData(cartDetailIds, null, null, member.getUserId(), response)
        );
    }

    @Test
    @DisplayName(value = "결제 요청 시 장바구니 상품 정보 조회. 장바구니 데이터가 사용자의 데이터가 아닌 경우")
    void getCartOrderDataNotEqualsMember() {
        List<Long> cartDetailIds = memberCartDetailList.stream().mapToLong(CartDetail::getId).boxed().toList();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThrows(
                CustomAccessDeniedException.class,
                () -> orderWriteUseCase.getCartOrderData(cartDetailIds, null, null, "noneUser", response)
        );
    }

    @Test
    @DisplayName(value = "결제 요청 시 장바구니 상품 정보 조회. 장바구니 데이터가 비회원 쿠키 값의 데이터가 아닌 경우")
    void getCartOrderDataNotEqualsAnonymous() {
        List<Long> cartDetailIds = anonymousCart.getCartDetailList().stream().mapToLong(CartDetail::getId).boxed().toList();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThrows(
                CustomAccessDeniedException.class,
                () -> orderWriteUseCase.getCartOrderData(cartDetailIds, null, new Cookie(cartCookieHeader, "wrongCookieValue"), anonymous.getUserId(), response)
        );
    }

    @Test
    @DisplayName(value = "결제 API 호출 직전 데이터 검증")
    void validateOrder() {
        Cookie orderToken = new Cookie("order", ORDER_TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();
        List<OrderItemVO> orderItemVOList = new ArrayList<>();
        List<OrderDataDTO> orderDataDTOList = new ArrayList<>();
        int totalPrice = 0;
        for(int i = 0; i < memberCartDetailList.size(); i++) {
            CartDetail detail = memberCartDetailList.get(i);
            int price = product.getProductPrice() * detail.getCartCount();
            totalPrice += price;

            orderItemVOList.add(
                    new OrderItemVO(
                            product.getId(),
                            detail.getProductOption().getId(),
                            detail.getCartCount(),
                            price
                    )
            );

            orderDataDTOList.add(
                    new OrderDataDTO(
                            product.getId(),
                            detail.getProductOption().getId(),
                            product.getProductName(),
                            detail.getProductOption().getSize(),
                            detail.getProductOption().getColor(),
                            detail.getCartCount(),
                            price
                    )
            );
        }
        PreOrderDataVO fixtureOrderDataVO = new PreOrderDataVO(member.getUserId(), orderItemVOList, totalPrice);
        OrderDataResponseDTO  requestDTO = new OrderDataResponseDTO(orderDataDTOList, totalPrice);

        orderRedisTemplate.opsForValue().set(ORDER_TOKEN, fixtureOrderDataVO);

        String result = assertDoesNotThrow(() -> orderWriteUseCase.validateOrderData(requestDTO, member.getUserId(), orderToken, response));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result);

        orderRedisTemplate.delete(ORDER_TOKEN);
    }

    @Test
    @DisplayName(value = "결제 API 호출 직전 데이터 검증. 주문 토큰이 없는 경우")
    void validateOrderTokenIsNull() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        List<OrderDataDTO> orderDataDTOList = new ArrayList<>();
        int totalPrice = 0;
        for(int i = 0; i < memberCartDetailList.size(); i++) {
            CartDetail detail = memberCartDetailList.get(i);
            int price = product.getProductPrice() * detail.getCartCount();
            totalPrice += price;

            orderDataDTOList.add(
                    new OrderDataDTO(
                            product.getId(),
                            detail.getProductOption().getId(),
                            product.getProductName(),
                            detail.getProductOption().getSize(),
                            detail.getProductOption().getColor(),
                            detail.getCartCount(),
                            price
                    )
            );
        }
        OrderDataResponseDTO  requestDTO = new OrderDataResponseDTO(orderDataDTOList, totalPrice);

        assertThrows(
                CustomOrderSessionExpiredException.class,
                () -> orderWriteUseCase.validateOrderData(requestDTO, member.getUserId(), null, response)
        );
    }

    @Test
    @DisplayName(value = "결제 API 호출 직전 데이터 검증. Redis 캐싱된 데이터가 없는 경우")
    void validateOrderCachingDataIsNull() {
        Cookie orderToken = new Cookie("order", ORDER_TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();
        List<OrderDataDTO> orderDataDTOList = new ArrayList<>();
        int totalPrice = 0;
        for(int i = 0; i < memberCartDetailList.size(); i++) {
            CartDetail detail = memberCartDetailList.get(i);
            int price = product.getProductPrice() * detail.getCartCount();
            totalPrice += price;

            orderDataDTOList.add(
                    new OrderDataDTO(
                            product.getId(),
                            detail.getProductOption().getId(),
                            product.getProductName(),
                            detail.getProductOption().getSize(),
                            detail.getProductOption().getColor(),
                            detail.getCartCount(),
                            price
                    )
            );
        }
        OrderDataResponseDTO  requestDTO = new OrderDataResponseDTO(orderDataDTOList, totalPrice);

        assertThrows(
                CustomOrderSessionExpiredException.class,
                () -> orderWriteUseCase.validateOrderData(requestDTO, member.getUserId(), orderToken, response)
        );
    }

    @Test
    @DisplayName(value = "결제 API 호출 직전 데이터 검증. 검증 결과 일치하지 않는 경우")
    void validateOrderFailedToValidate() {
        Cookie orderToken = new Cookie("order", ORDER_TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();
        List<OrderItemVO> orderItemVOList = new ArrayList<>();
        List<OrderDataDTO> orderDataDTOList = new ArrayList<>();
        int totalPrice = 0;
        for(int i = 0; i < memberCartDetailList.size(); i++) {
            CartDetail detail = memberCartDetailList.get(i);
            int price = product.getProductPrice() * detail.getCartCount();
            totalPrice += price;

            orderItemVOList.add(
                    new OrderItemVO(
                            product.getId(),
                            detail.getProductOption().getId(),
                            detail.getCartCount(),
                            price
                    )
            );

            orderDataDTOList.add(
                    new OrderDataDTO(
                            product.getId(),
                            detail.getProductOption().getId(),
                            product.getProductName(),
                            detail.getProductOption().getSize(),
                            detail.getProductOption().getColor(),
                            detail.getCartCount(),
                            price
                    )
            );
        }
        PreOrderDataVO fixtureOrderDataVO = new PreOrderDataVO(member.getUserId(), orderItemVOList, totalPrice + 1000);
        OrderDataResponseDTO  requestDTO = new OrderDataResponseDTO(orderDataDTOList, totalPrice);

        orderRedisTemplate.opsForValue().set(ORDER_TOKEN, fixtureOrderDataVO);

        assertThrows(
                CustomOrderSessionExpiredException.class,
                () -> orderWriteUseCase.validateOrderData(requestDTO, member.getUserId(), orderToken, response)
        );

        orderRedisTemplate.delete(ORDER_TOKEN);
    }
}
