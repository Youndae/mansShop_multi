package com.example.modulecart.usecase.integration;

import com.example.moduleauth.repository.MemberRepository;
import com.example.modulecart.ModuleCartApplication;
import com.example.modulecart.model.dto.out.CartDetailDTO;
import com.example.modulecart.repository.CartRepository;
import com.example.modulecart.usecase.CartReadUseCase;
import com.example.modulecommon.fixture.CartFixture;
import com.example.modulecommon.fixture.ClassificationFixture;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.*;
import com.example.moduleproduct.repository.classification.ClassificationRepository;
import com.example.moduleproduct.repository.product.ProductRepository;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ModuleCartApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@Transactional
public class CartReadUseCaseIT {

    @Autowired
    private CartReadUseCase cartReadUseCase;

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
    private EntityManager em;

    private List<Member> memberList;

    private Member noneCartMember;

    private Member anonymous;

    private List<Product> productList;

    private List<ProductOption> optionList;

    private List<Cart> cartList;

    private Cart anonymousCart;

    @Value("#{jwt['cookie.cart.header']}")
    private String cartCookieHeader;

    private Cookie anonymousCartCookie;

    @BeforeEach
    void init() {
        MemberAndAuthFixtureDTO memberAndAuthFixture = MemberAndAuthFixture.createDefaultMember(30);
        MemberAndAuthFixtureDTO anonymousFixture = MemberAndAuthFixture.createAnonymous();
        MemberAndAuthFixtureDTO noneCartMemberFixture = MemberAndAuthFixture.createDefaultMember(1);
        memberList = memberAndAuthFixture.memberList();
        anonymous = anonymousFixture.memberList().get(0);
        noneCartMember = noneCartMemberFixture.memberList().get(0);

        List<Member> saveMemberList = new ArrayList<>(memberList);
        saveMemberList.add(anonymous);
        saveMemberList.add(noneCartMember);

        memberRepository.saveAll(saveMemberList);

        List<Classification> classificationList = ClassificationFixture.createClassifications();
        classificationRepository.saveAll(classificationList);

        productList = ProductFixture.createProductFixtureList(10, classificationList.get(0));
        optionList = productList.stream().flatMap(v -> v.getProductOptions().stream()).toList();

        productRepository.saveAll(productList);
        productOptionRepository.saveAll(optionList);

        anonymousCartCookie = new Cookie(cartCookieHeader, "testAnonymousCookieValue");

        cartList = CartFixture.createDefaultMemberCart(memberList, optionList);
        anonymousCart = CartFixture.createSaveAnonymousCart(optionList.get(0), anonymous, anonymousCartCookie.getValue());

        List<Cart> saveCartList = new ArrayList<>(cartList);
        saveCartList.add(anonymousCart);

        cartRepository.saveAll(saveCartList);

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName(value = "장바구니 목록 조회. 회원인 경우")
    void getCartListByUser() {
        Member member = memberList.get(0);
        Cart memberCart = cartList.stream().filter(v -> v.getMember().getUserId().equals(member.getUserId())).findFirst().get();
        List<CartDetail> memberCartDetailList = memberCart.getCartDetailList().stream().sorted(Comparator.comparingLong(CartDetail::getId).reversed()).toList();

        List<CartDetailDTO> result = assertDoesNotThrow(() -> cartReadUseCase.getCartList(null, member.getUserId()));

        assertNotNull(result);
        assertEquals(memberCartDetailList.size(), result.size());

        for(int i = 0; i < result.size(); i++) {
            CartDetailDTO resultDTO = result.get(i);
            CartDetail fixture = memberCartDetailList.get(i);
            Product fixtureProduct = fixture.getProductOption().getProduct();
            int originPrice = fixtureProduct.getProductPrice() * fixture.getCartCount();
            int price = (fixtureProduct.getProductPrice() - (fixtureProduct.getProductPrice() * fixtureProduct.getProductDiscount() / 100)) * fixture.getCartCount();

            assertEquals(fixture.getId(), resultDTO.cartDetailId());
            assertEquals(fixtureProduct.getId(), resultDTO.productId());
            assertEquals(fixture.getProductOption().getId(), resultDTO.optionId());
            assertEquals(fixtureProduct.getProductName(), resultDTO.productName());
            assertEquals(fixtureProduct.getThumbnail(), resultDTO.productThumbnail());
            assertEquals(fixture.getProductOption().getSize(), resultDTO.size());
            assertEquals(fixture.getProductOption().getColor(), resultDTO.color());
            assertEquals(fixture.getCartCount(), resultDTO.count());
            assertEquals(originPrice, resultDTO.originPrice());
            assertEquals(price, resultDTO.price());
            assertEquals(fixtureProduct.getProductDiscount(), resultDTO.discount());
        }
    }

    @Test
    @DisplayName(value = "장바구니 목록 조회. 비회원인 경우")
    void getCartListByAnonymous() {
        List<CartDetail> memberCartDetailList  = anonymousCart.getCartDetailList().stream().sorted(Comparator.comparingLong(CartDetail::getId).reversed()).toList();

        List<CartDetailDTO> result = assertDoesNotThrow(() -> cartReadUseCase.getCartList(anonymousCartCookie, null));

        assertNotNull(result);
        assertEquals(memberCartDetailList.size(), result.size());

        for(int i = 0; i < result.size(); i++) {
            CartDetailDTO resultDTO = result.get(i);
            CartDetail fixture = memberCartDetailList.get(i);
            Product fixtureProduct = fixture.getProductOption().getProduct();
            int originPrice = fixtureProduct.getProductPrice() * fixture.getCartCount();
            int price = (fixtureProduct.getProductPrice() - (fixtureProduct.getProductPrice() * fixtureProduct.getProductDiscount() / 100)) * fixture.getCartCount();

            assertEquals(fixture.getId(), resultDTO.cartDetailId());
            assertEquals(fixtureProduct.getId(), resultDTO.productId());
            assertEquals(fixture.getProductOption().getId(), resultDTO.optionId());
            assertEquals(fixtureProduct.getProductName(), resultDTO.productName());
            assertEquals(fixtureProduct.getThumbnail(), resultDTO.productThumbnail());
            assertEquals(fixture.getProductOption().getSize(), resultDTO.size());
            assertEquals(fixture.getProductOption().getColor(), resultDTO.color());
            assertEquals(fixture.getCartCount(), resultDTO.count());
            assertEquals(originPrice, resultDTO.originPrice());
            assertEquals(price, resultDTO.price());
            assertEquals(fixtureProduct.getProductDiscount(), resultDTO.discount());
        }
    }

    @Test
    @DisplayName(value = "장바구니 목록 조회. 데이터가 없는 경우")
    void getCartListEmpty() {
        Member member = memberList.get(0);
        cartRepository.deleteAll();

        List<CartDetailDTO> result = assertDoesNotThrow(() -> cartReadUseCase.getCartList(null, member.getUserId()));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
