package com.example.moduletest.integration.order.useCase;

import com.example.moduleadmin.repository.PeriodSalesSummaryRepository;
import com.example.moduleadmin.repository.ProductSalesSummaryRepository;
import com.example.modulecart.repository.CartDetailRepository;
import com.example.modulecart.repository.CartRepository;
import com.example.modulecommon.fixture.CartFixture;
import com.example.modulecommon.fixture.ClassificationFixture;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleorder.model.dto.in.OrderProductDTO;
import com.example.moduleorder.model.dto.in.PaymentDTO;
import com.example.moduleorder.model.vo.OrderItemVO;
import com.example.moduleorder.model.vo.PreOrderDataVO;
import com.example.moduleorder.repository.ProductOrderDetailRepository;
import com.example.moduleorder.repository.ProductOrderRepository;
import com.example.moduleorder.usecase.OrderWriteUseCase;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import com.example.moduletest.ModuleTestApplication;
import com.example.moduleuser.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ModuleTestApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
public class OrderWriteUseCaseIT {

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
    private ProductOrderRepository productOrderRepository;

    @Autowired
    private ProductOrderDetailRepository productOrderDetailRepository;

    @Autowired
    private PeriodSalesSummaryRepository periodSalesSummaryRepository;

    @Autowired
    private ProductSalesSummaryRepository productSalesSummaryRepository;

    @Autowired
    private CartDetailRepository cartDetailRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private RedisTemplate<String, PreOrderDataVO> orderRedisTemplate;

    @Autowired
    private EntityManager em;

    private Member member;

    private Member anonymous;

    private Product product;

    private List<ProductOption> productOptionList;

    private Cart memberCart;

    private List<CartDetail> memberCartDetailList;

    private Cart anonymousCart;

    private static final String ANONYMOUS_COOKIE_VALUE = "testCookieValue";

    private static final String ORDER_TOKEN_HEADER = "order";

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

        memberCartDetailList = memberCart.getCartDetailList();
    }

    @AfterEach
    void cleanUp() {
        cartDetailRepository.deleteAll();
        cartRepository.deleteAll();
        productOrderDetailRepository.deleteAll();
        productOrderRepository.deleteAll();
        memberRepository.deleteAll();
        productSalesSummaryRepository.deleteAll();
        productOptionRepository.deleteAll();
        productRepository.deleteAll();
        classificationRepository.deleteAll();
        periodSalesSummaryRepository.deleteAll();

        orderRedisTemplate.delete(ORDER_TOKEN);
    }

    private List<OrderProductDTO> createDirectOrderProductDTOFixtureList() {
        List<OrderProductDTO> resultList = new ArrayList<>();
        for(int i = 0; i < productOptionList.size(); i++) {
            ProductOption option = productOptionList.get(i);

            resultList.add(
                    new OrderProductDTO(
                            option.getId(),
                            product.getProductName(),
                            product.getId(),
                            i + 1,
                            option.getProduct().getProductPrice() * (i + 1)
                    )
            );
        }

        return resultList;
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

    @Test
    @DisplayName(value = "결제 이후 주문 데이터 처리. 비회원이 상품 상세 페이지에서 주문한 경우")
    void paymentDirectAnonymous() {
        List<OrderProductDTO> orderProductList = createDirectOrderProductDTOFixtureList();
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
                "card",
                "direct"
        );
        List<OrderItemVO> orderItemVOList = new ArrayList<>();
        Map<String, Long> productCountMap = new HashMap<>();
        productCountMap.put(product.getId(), product.getProductSalesQuantity());
        Map<Long, Integer> optionCountMap = productOptionList.stream()
                .collect(Collectors.toMap(
                        ProductOption::getId,
                        ProductOption::getStock
                ));

        for(OrderProductDTO dto : orderProductList) {
            orderItemVOList.add(
                    new OrderItemVO(
                            dto.getProductId(),
                            dto.getOptionId(),
                            dto.getDetailCount(),
                            dto.getDetailPrice()
                    )
            );

            productCountMap.put(
                    dto.getProductId(),
                    productCountMap.get(dto.getProductId()) + dto.getDetailCount()
            );

            int optionStock = optionCountMap.get(dto.getOptionId()) - dto.getDetailCount();

            if(optionStock < 0)
                optionStock = 0;

            optionCountMap.put(
                    dto.getOptionId(),
                    optionStock
            );
        }

        PreOrderDataVO cachingOrderDataVO = new PreOrderDataVO(anonymous.getUserId(), orderItemVOList, totalPrice);
        orderRedisTemplate.opsForValue().set(ORDER_TOKEN, cachingOrderDataVO);
        Cookie orderToken = new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();

        String result = assertDoesNotThrow(() -> orderWriteUseCase.orderDataProcessAfterPayment(paymentDTO, null, null, orderToken, response));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result);

        List<ProductOrder> saveOrder = productOrderRepository.findAll();
        assertFalse(saveOrder.isEmpty());
        assertEquals(1, saveOrder.size());

        List<ProductOrderDetail> saveOrderDetail = productOrderDetailRepository.findAll();
        assertEquals(productOptionList.size(), saveOrderDetail.size());

        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {

                    List<PeriodSalesSummary> savePeriodSummary = periodSalesSummaryRepository.findAll();

                    assertFalse(savePeriodSummary.isEmpty());
                    assertEquals(1, savePeriodSummary.size());
                    assertEquals(totalPrice, savePeriodSummary.get(0).getCardTotal());
                    assertEquals(0, savePeriodSummary.get(0).getCashTotal());

                    List<ProductSalesSummary> saveProductSummary = productSalesSummaryRepository.findAll();

                    assertFalse(saveProductSummary.isEmpty());
                    assertEquals(productOptionList.size(), saveProductSummary.size());

                    List<Product> patchProductList = productRepository.findAll();
                    for(Product data : patchProductList) {
                        if(productCountMap.containsKey(data.getId())) {
                            assertEquals(productCountMap.get(data.getId()), data.getProductSalesQuantity());
                        }
                    }

                    List<ProductOption> patchProductOptionList = productOptionRepository.findAll();
                    for(ProductOption data : patchProductOptionList) {
                        if(optionCountMap.containsKey(data.getId())) {
                            assertEquals(optionCountMap.get(data.getId()), data.getStock());
                        }
                    }
                });
    }

    @Test
    @DisplayName(value = "결제 이후 주문 데이터 처리. 회원이 장바구니에서 주문한 경우")
    void paymentCartMember() {
        List<Cart> testCart = cartRepository.findAll();
        System.out.println("testCart size : " + testCart.size());
        System.out.println("testCart1 user : " + testCart.get(0).getMember().getUserId());
        System.out.println("testCart1 id : " + testCart.get(0).getId());
        System.out.println("testCart2 user : " + testCart.get(1).getMember().getUserId());
        System.out.println("testCart2 id : " + testCart.get(1).getId());
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
        List<OrderItemVO> orderItemVOList = new ArrayList<>();
        Map<String, Long> productCountMap = new HashMap<>();
        productCountMap.put(product.getId(), product.getProductSalesQuantity());
        Map<Long, Integer> optionCountMap = productOptionList.stream()
                .collect(Collectors.toMap(
                        ProductOption::getId,
                        ProductOption::getStock
                ));

        for(OrderProductDTO dto : orderProductList) {
            orderItemVOList.add(
                    new OrderItemVO(
                            dto.getProductId(),
                            dto.getOptionId(),
                            dto.getDetailCount(),
                            dto.getDetailPrice()
                    )
            );

            productCountMap.put(
                    dto.getProductId(),
                    productCountMap.get(dto.getProductId()) + dto.getDetailCount()
            );

            int optionStock = optionCountMap.get(dto.getOptionId()) - dto.getDetailCount();

            if(optionStock < 0)
                optionStock = 0;

            optionCountMap.put(
                    dto.getOptionId(),
                    optionStock
            );
        }
        PreOrderDataVO cachingOrderDataVO = new PreOrderDataVO(member.getUserId(), orderItemVOList, totalPrice);
        orderRedisTemplate.opsForValue().set(ORDER_TOKEN, cachingOrderDataVO);
        Cookie orderToken = new Cookie(ORDER_TOKEN_HEADER, ORDER_TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();

        String result = assertDoesNotThrow(() -> orderWriteUseCase.orderDataProcessAfterPayment(paymentDTO, null, member.getUserId(), orderToken, response));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result);

        List<ProductOrder> saveOrder = productOrderRepository.findAll();
        assertFalse(saveOrder.isEmpty());
        assertEquals(1, saveOrder.size());

        List<ProductOrderDetail> saveOrderDetail = productOrderDetailRepository.findAll();
        assertEquals(productOptionList.size(), saveOrderDetail.size());

        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    List<Cart> cart = cartRepository.findAll();
                    assertEquals(1, cart.size());

                    List<PeriodSalesSummary> savePeriodSummary = periodSalesSummaryRepository.findAll();

                    assertFalse(savePeriodSummary.isEmpty());
                    assertEquals(1, savePeriodSummary.size());
                    assertEquals(0, savePeriodSummary.get(0).getCardTotal());
                    assertEquals(totalPrice, savePeriodSummary.get(0).getCashTotal());

                    List<ProductSalesSummary> saveProductSummary = productSalesSummaryRepository.findAll();

                    assertFalse(saveProductSummary.isEmpty());
                    assertEquals(productOptionList.size(), saveProductSummary.size());

                    List<Product> patchProductList = productRepository.findAll();
                    for(Product data : patchProductList) {
                        if(productCountMap.containsKey(data.getId())) {
                            assertEquals(productCountMap.get(data.getId()), data.getProductSalesQuantity());
                        }
                    }

                    List<ProductOption> patchProductOptionList = productOptionRepository.findAll();
                    for(ProductOption data : patchProductOptionList) {
                        if(optionCountMap.containsKey(data.getId())) {
                            assertEquals(optionCountMap.get(data.getId()), data.getStock());
                        }
                    }
                });
    }
}
