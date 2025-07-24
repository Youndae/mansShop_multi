package com.example.modulecart.usecase.integration;

import com.example.moduleauth.repository.MemberRepository;
import com.example.modulecart.ModuleCartApplication;
import com.example.modulecart.model.dto.business.CartMemberDTO;
import com.example.modulecart.model.dto.in.AddCartDTO;
import com.example.modulecart.model.dto.out.CartCookieResponseDTO;
import com.example.modulecart.model.dto.out.CartDetailDTO;
import com.example.modulecart.repository.CartDetailRepository;
import com.example.modulecart.repository.CartRepository;
import com.example.modulecart.usecase.CartWriteUseCase;
import com.example.modulecommon.customException.CustomNotFoundException;
import com.example.modulecommon.fixture.CartFixture;
import com.example.modulecommon.fixture.ClassificationFixture;
import com.example.modulecommon.fixture.MemberAndAuthFixture;
import com.example.modulecommon.fixture.ProductFixture;
import com.example.modulecommon.model.dto.MemberAndAuthFixtureDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.Result;
import com.example.modulecommon.model.enumuration.Role;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ModuleCartApplication.class)
@EntityScan(basePackages = "com.example.modulecommon")
@EnableJpaRepositories(basePackages = "com.example")
@ComponentScan(basePackages = {"com.example.moduleconfig", "com.example.modulecommon"})
@ActiveProfiles("test")
@Transactional
public class CartWriteUseCaseIT {

    @Autowired
    private CartWriteUseCase cartWriteUseCase;

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
    @DisplayName(value = "장바구니 추가. 장바구니 데이터가 존재하지 않고 처음 추가하는 경우")
    void addCart() {
        Member member = memberList.get(0);
        cartDetailRepository.deleteAll();
        cartRepository.deleteAll();
        int optionCount = 3;
        List<Long> optionIds = optionList.stream().limit(optionCount).mapToLong(ProductOption::getId).boxed().toList();
        List<AddCartDTO> addList = new ArrayList<>();
        for(int i = 1; i < optionCount; i++) {
            addList.add(
                    new AddCartDTO(
                            optionIds.get(i),
                            i
                    )
            );
        }

        CartCookieResponseDTO result = assertDoesNotThrow(
                () -> cartWriteUseCase.addProductForCart(addList, null, member.getUserId())
        );

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result.responseMessage());
        assertNull(result.cookieValue());

        CartMemberDTO cartMemberDTO = new CartMemberDTO(member.getUserId(), null);
        Long saveCartId = cartRepository.findIdByUserId(cartMemberDTO);
        assertNotNull(saveCartId);

        List<CartDetailDTO> detailList = cartDetailRepository.findAllByCartId(saveCartId);
        assertEquals(addList.size(), detailList.size());
    }

    @Test
    @DisplayName(value = "장바구니 추가. 장바구니가 존재하고 추가하는 경우")
    void addCartExists() {
        Member member = memberList.get(0);
        cartDetailRepository.deleteAll();
        int optionCount = 3;
        List<Long> optionIds = optionList.stream().limit(optionCount).mapToLong(ProductOption::getId).boxed().toList();
        List<AddCartDTO> addList = new ArrayList<>();
        for(int i = 1; i < optionCount; i++) {
            addList.add(
                    new AddCartDTO(
                            optionIds.get(i),
                            i
                    )
            );
        }
        Long memberCartId = cartList.stream()
                .filter(v -> v.getMember().getUserId().equals(member.getUserId()))
                .findFirst()
                .get()
                .getId();

        CartCookieResponseDTO result = assertDoesNotThrow(() -> cartWriteUseCase.addProductForCart(addList, null, member.getUserId()));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result.responseMessage());
        assertNull(result.cookieValue());

        List<CartDetailDTO> detailList = cartDetailRepository.findAllByCartId(memberCartId);
        assertFalse(detailList.isEmpty());
        assertEquals(addList.size(), detailList.size());
    }

    @Test
    @DisplayName(value = "장바구니 추가. 비회원이 처음 장바구니 추가를 하는 경우 쿠키 반환")
    void addCartAnonymous() {
        cartDetailRepository.deleteAll();
        cartRepository.deleteAll();
        int optionCount = 3;
        List<Long> optionIds = optionList.stream().limit(optionCount).mapToLong(ProductOption::getId).boxed().toList();
        List<AddCartDTO> addList = new ArrayList<>();
        for(int i = 1; i < optionCount; i++) {
            addList.add(
                    new AddCartDTO(
                            optionIds.get(i),
                            i
                    )
            );
        }

        CartCookieResponseDTO result = assertDoesNotThrow(() -> cartWriteUseCase.addProductForCart(addList, null, null));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result.responseMessage());
        assertNotNull(result.cookieValue());

        CartMemberDTO cartMemberDTO = new CartMemberDTO(Role.ANONYMOUS.getRole(), result.cookieValue());

        Long saveCartId = cartRepository.findIdByUserId(cartMemberDTO);
        assertNotNull(saveCartId);

        List<CartDetailDTO> detailList = cartDetailRepository.findAllByCartId(saveCartId);
        assertEquals(addList.size(), detailList.size());
    }

    @Test
    @DisplayName(value = "장바구니 추가. 추가하는 상품 옵션이 존재하지 않는 경우")
    void addCartCookieProductOptionNotFound() {
        Member member = memberList.get(0);
        List<AddCartDTO> addList = List.of(new AddCartDTO(0L, 3));

        assertThrows(
                IllegalArgumentException.class,
                () -> cartWriteUseCase.addProductForCart(addList, null, member.getUserId())
        );
    }

    @Test
    @DisplayName(value = "장바구니 수량 증가")
    void countUp() {
        Member member = memberList.get(0);
        Cart memberCart = cartList.stream().filter(v -> v.getMember().getUserId().equals(member.getUserId())).findFirst().get();
        long detailId = memberCart.getCartDetailList().get(0).getId();
        int detailCount = memberCart.getCartDetailList().get(0).getCartCount();

        CartCookieResponseDTO result = assertDoesNotThrow(() -> cartWriteUseCase.cartCountUp(detailId, null, member.getUserId()));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result.responseMessage());
        assertNull(result.cookieValue());

        CartDetail patchDetail = cartDetailRepository.findById(detailId).orElse(null);
        assertNotNull(patchDetail);
        assertEquals(detailCount + 1, patchDetail.getCartCount());
    }

    @Test
    @DisplayName(value = "장바구니 수량 증가. 사용자 장바구니가 존재하지 않는 경우")
    void countUpCartNotFound() {
        assertThrows(
                CustomNotFoundException.class,
                () -> cartWriteUseCase.cartCountUp(1L, null, "noneMember")
        );
    }

    @Test
    @DisplayName(value = "장바구니 수량 증가. 사용자 장바구니 상세 데이터가 존재하지 않는 경우")
    void countUpCartDetailNotFound() {
        Member member = memberList.get(0);

        assertThrows(
                IllegalArgumentException.class,
                () -> cartWriteUseCase.cartCountUp(0L, null, member.getUserId())
        );
    }

    @Test
    @DisplayName(value = "장바구니 수량 감소")
    void countDown() {
        Member member = memberList.get(0);
        Cart memberCart = cartList.stream().filter(v -> v.getMember().getUserId().equals(member.getUserId())).findFirst().get();
        long detailId = memberCart.getCartDetailList().get(0).getId();
        int detailCount = memberCart.getCartDetailList().get(0).getCartCount();
        int detailCountResult = detailCount == 1 ? 1 : detailCount - 1;

        CartCookieResponseDTO result = assertDoesNotThrow(() -> cartWriteUseCase.cartCountDown(detailId, null, member.getUserId()));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result.responseMessage());
        assertNull(result.cookieValue());

        CartDetail patchDetail = cartDetailRepository.findById(detailId).orElse(null);
        assertNotNull(patchDetail);
        assertEquals(detailCountResult, patchDetail.getCartCount());
    }

    @Test
    @DisplayName(value = "장바구니 수량 감소. 사용자 장바구니가 존재하지 않는 경우")
    void countDownCartNotFound() {
        assertThrows(
                CustomNotFoundException.class,
                () -> cartWriteUseCase.cartCountDown(1L, null, "noneMember")
        );
    }

    @Test
    @DisplayName(value = "장바구니 수량 감소. 사용자 장바구니 상세 데이터가 존재하지 않는 경우")
    void countDownCartDetailNotFound() {
        Member member = memberList.get(0);

        assertThrows(
                IllegalArgumentException.class,
                () -> cartWriteUseCase.cartCountDown(0L, null, member.getUserId())
        );
    }

    @Test
    @DisplayName(value = "선택 상품 제거")
    void deleteCartSelect() {
        Member member = memberList.get(0);
        Cart memberCart = cartList.stream().filter(v -> v.getMember().getUserId().equals(member.getUserId())).findFirst().get();
        List<Long> detailIds = memberCart.getCartDetailList()
                .stream()
                .limit(memberCart.getCartDetailList().size() - 1)
                .mapToLong(CartDetail::getId)
                .boxed()
                .toList();

        CartCookieResponseDTO result = assertDoesNotThrow(() -> cartWriteUseCase.deleteSelectProductFromCart(detailIds, null, member.getUserId()));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result.responseMessage());
        assertNull(result.cookieValue());

        List<Long> memberCartDetailIds = cartDetailRepository.findAllIdByCartId(memberCart.getId());
        assertFalse(memberCartDetailIds.isEmpty());
        assertEquals(1, memberCartDetailIds.size());
    }

    @Test
    @DisplayName(value = "선택 상품 제거. 사용자 장바구니가 존재하지 않는 경우")
    void deleteCartSelectCartNotFound() {
        assertThrows(
                CustomNotFoundException.class,
                () -> cartWriteUseCase.deleteSelectProductFromCart(List.of(1L), null, "noneMember")
        );
    }

    @Test
    @DisplayName(value = "선택 상품 제거. 삭제할 장바구니 상세 데이터가 존재하지 않는 경우")
    void deleteCartSelectCartDetailNotFound() {
        Member member = memberList.get(0);

        assertThrows(
                IllegalArgumentException.class,
                () -> cartWriteUseCase.deleteSelectProductFromCart(List.of(0L), null, member.getUserId())
        );
    }

    @Test
    @DisplayName(value = "장바구니 전체 삭제")
    void deleteAllCart() {
        Member member = memberList.get(0);

        String result = assertDoesNotThrow(() -> cartWriteUseCase.deleteAllProductFromCart(null, member.getUserId()));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result);

        CartMemberDTO cartMemberDTO = new CartMemberDTO(member.getUserId(), null);
        Long cartId = cartRepository.findIdByUserId(cartMemberDTO);
        assertNull(cartId);
    }

    @Test
    @DisplayName(value = "장바구니 전체 삭제. 장바구니 데이터가 존재하지 않는 경우")
    void deleteAllCartNotFound() {
        assertThrows(
                CustomNotFoundException.class,
                () -> cartWriteUseCase.deleteAllProductFromCart(null, "noneMember")
        );
    }
}
