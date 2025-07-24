package com.example.modulecart.service.unit;

import com.example.modulecart.model.dto.business.CartMemberDTO;
import com.example.modulecart.model.dto.in.AddCartDTO;
import com.example.modulecart.service.CartDomainService;
import com.example.modulecommon.model.entity.Cart;
import com.example.modulecommon.model.entity.CartDetail;
import com.example.modulecommon.model.entity.ProductOption;
import com.example.modulecommon.model.enumuration.Role;
import com.example.moduleproduct.repository.productOption.ProductOptionRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartDomainServiceUnitTest {

    @InjectMocks
    private CartDomainService cartDomainService;

    @Mock
    private ProductOptionRepository productOptionRepository;

    @Test
    @DisplayName(value = "CartMemberDTO 생성. 회원인 경우")
    void setCartMemberDTOToUser() {
        String userId = "testUserId";

        CartMemberDTO result = assertDoesNotThrow(() -> cartDomainService.setCartMemberDTO(null, userId));

        assertNotNull(result);
        assertEquals(userId, result.uid());
        assertNull(result.cartCookieValue());
    }

    @Test
    @DisplayName(value = "CartMemberDTO 생성. 비회원이며 쿠키가 있는 경우")
    void setCartMemberDTOToAnonymousCookie() {
        Cookie cookie = new Cookie("testCookie", "testCookieValue");

        CartMemberDTO result = assertDoesNotThrow(() -> cartDomainService.setCartMemberDTO(cookie, null));

        assertNotNull(result);
        assertEquals(Role.ANONYMOUS.getRole(), result.uid());
        assertEquals(cookie.getValue(), result.cartCookieValue());
    }

    @Test
    @DisplayName(value = "CartMemberDTO 생성. 비회원이며 쿠키가 없는 경우")
    void setCartMemberDTOToAllParameterIsNull() {
        CartMemberDTO result = assertDoesNotThrow(() -> cartDomainService.setCartMemberDTO(null, null));

        assertNotNull(result);
        assertEquals(Role.ANONYMOUS.getRole(), result.uid());
        assertNotNull(result.cartCookieValue());
    }

    @Test
    @DisplayName(value = "장바구니 추가 시 cartDetail 추가 혹은 이미 존재한다면 수량 증가")
    void mapCartAndCartDetails() {
        AddCartDTO addCartDTO1 = new AddCartDTO(1L, 2);
        AddCartDTO addCartDTO2 = new AddCartDTO(2L, 2);
        Cart cart = Cart.builder().id(1L).build();
        ProductOption productOption1 = ProductOption.builder().id(1L).build();
        ProductOption productOption2 = ProductOption.builder().id(2L).build();
        CartDetail savedCartDetail = CartDetail.builder().id(1L).productOption(productOption1).cartCount(5).build();
        List<AddCartDTO> addList = List.of(addCartDTO1, addCartDTO2);
        List<CartDetail> savedDetails = new ArrayList<>(List.of(savedCartDetail));

        when(productOptionRepository.findById(1L)).thenReturn(Optional.of(productOption1));
        when(productOptionRepository.findById(2L)).thenReturn(Optional.of(productOption2));

        assertDoesNotThrow(() -> cartDomainService.mapCartAndCartDetails(addList, cart, savedDetails));

        List<CartDetail> resultDetails = cart.getCartDetailList();
        assertNotNull(resultDetails);
        assertFalse(resultDetails.isEmpty());
        assertEquals(2, resultDetails.size());
        assertEquals(7, resultDetails.get(0).getCartCount());
        assertEquals(2, resultDetails.get(1).getCartCount());
    }

    @Test
    @DisplayName(value = "장바구니 추가 시 cartDetail 추가 혹은 이미 존재한다면 수량 증가. savedDetails가 emptyList인 경우")
    void mapCartAndCartDetailsEmptyList() {
        AddCartDTO addCartDTO1 = new AddCartDTO(1L, 2);
        AddCartDTO addCartDTO2 = new AddCartDTO(2L, 2);
        Cart cart = Cart.builder().id(1L).build();
        ProductOption productOption1 = ProductOption.builder().id(1L).build();
        ProductOption productOption2 = ProductOption.builder().id(2L).build();
        List<AddCartDTO> addList = List.of(addCartDTO1, addCartDTO2);
        List<CartDetail> savedDetails = Collections.emptyList();

        when(productOptionRepository.findById(1L)).thenReturn(Optional.of(productOption1));
        when(productOptionRepository.findById(2L)).thenReturn(Optional.of(productOption2));

        assertDoesNotThrow(() -> cartDomainService.mapCartAndCartDetails(addList, cart, savedDetails));

        List<CartDetail> resultDetails = cart.getCartDetailList();
        assertNotNull(resultDetails);
        assertFalse(resultDetails.isEmpty());
        assertEquals(2, resultDetails.size());
        resultDetails.forEach(v -> assertEquals(2, v.getCartCount()));
    }

    @Test
    @DisplayName(value = "장바구니 추가 시 cartDetail 추가 혹은 이미 존재한다면 수량 증가. productOption 조회 결과가 null인 경우")
    void mapCartAndCartDetailsOptionNotFound() {
        AddCartDTO addCartDTO1 = new AddCartDTO(1L, 2);
        AddCartDTO addCartDTO2 = new AddCartDTO(2L, 2);
        Cart cart = Cart.builder().id(1L).build();
        ProductOption productOption1 = ProductOption.builder().id(1L).build();
        CartDetail savedCartDetail = CartDetail.builder().id(1L).productOption(productOption1).cartCount(5).build();
        List<AddCartDTO> addList = List.of(addCartDTO1, addCartDTO2);
        List<CartDetail> savedDetails = new ArrayList<>(List.of(savedCartDetail));

        when(productOptionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> cartDomainService.mapCartAndCartDetails(addList, cart, savedDetails)
        );
    }
}
