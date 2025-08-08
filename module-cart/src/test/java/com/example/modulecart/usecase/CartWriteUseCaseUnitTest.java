package com.example.modulecart.usecase;


import com.example.modulecart.model.dto.business.CartMemberDTO;
import com.example.modulecart.model.dto.in.AddCartDTO;
import com.example.modulecart.model.dto.out.CartCookieResponseDTO;
import com.example.modulecart.service.CartDataService;
import com.example.modulecart.service.CartDomainService;
import com.example.modulecommon.customException.CustomNotFoundException;
import com.example.modulecommon.model.entity.Cart;
import com.example.modulecommon.model.entity.CartDetail;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.ProductOption;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleuser.service.UserDataService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartWriteUseCaseUnitTest {

    @InjectMocks
    private CartWriteUseCase cartWriteUseCase;

    @Mock
    private CartDataService cartDataService;

    @Mock
    private CartDomainService cartDomainService;

    @Mock
    private UserDataService userDataService;

    private CartMemberDTO getCartMemberDTO() {
        return new CartMemberDTO("tester", null);
    }

    @Test
    @DisplayName(value = "장바구니에 상품 추가. 장바구니가 이미 존재하고 상품이 추가되는 경우")
    void addProductForCart() {
        CartMemberDTO cartMemberDTO = getCartMemberDTO();
        Cart userCart = Cart.builder().id(1L).build();
        CartDetail userCartDetail = CartDetail.builder()
                .id(1L)
                .productOption(ProductOption.builder().id(1L).build())
                .build();
        userCart.addCartDetail(userCartDetail);
        AddCartDTO addCartDTO1 = new AddCartDTO(1L, 2);
        AddCartDTO addCartDTO2 = new AddCartDTO(2L, 2);
        List<AddCartDTO> addCartDTOList = List.of(addCartDTO1, addCartDTO2);
        List<CartDetail> userCartDetailList = new ArrayList<>(List.of(userCartDetail));
        List<Long> addOptionIds = List.of(1L, 2L);

        when(cartDomainService.setCartMemberDTO(any(), any())).thenReturn(cartMemberDTO);
        when(cartDataService.getCartData(any(CartMemberDTO.class))).thenReturn(userCart);
        when(cartDomainService.mapAddCartOptionIds(anyList())).thenReturn(addOptionIds);
        when(cartDataService.findAllUserCartDetails(anyLong(), anyList())).thenReturn(userCartDetailList);
        doNothing().when(cartDomainService).mapCartAndCartDetails(anyList(), any(Cart.class), anyList());
        doNothing().when(cartDataService).saveCart(any(Cart.class));

        CartCookieResponseDTO result = assertDoesNotThrow(() -> cartWriteUseCase.addProductForCart(addCartDTOList, null, cartMemberDTO.uid()));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result.responseMessage());
        assertNull(result.cookieValue());
    }

    @Test
    @DisplayName(value = "장바구니에 상품 추가. 장바구니가 존재하지 않고 상품이 추가되는 경우")
    void addProductForCartNewCart() {
        CartMemberDTO cartMemberDTO = getCartMemberDTO();
        AddCartDTO addCartDTO1 = new AddCartDTO(1L, 2);
        AddCartDTO addCartDTO2 = new AddCartDTO(2L, 2);
        Cart userCart = Cart.builder().id(1L).build();
        List<AddCartDTO> addCartDTOList = List.of(addCartDTO1, addCartDTO2);

        when(cartDomainService.setCartMemberDTO(any(), any())).thenReturn(cartMemberDTO);
        when(cartDataService.getCartData(any(CartMemberDTO.class))).thenReturn(null);
        when(userDataService.getMemberByUserIdOrElseNull(any())).thenReturn(Member.builder().userId(cartMemberDTO.uid()).build());
        when(cartDomainService.buildCart(any(Member.class), any())).thenReturn(userCart);
        doNothing().when(cartDomainService).mapCartAndCartDetails(anyList(), any(Cart.class), anyList());
        doNothing().when(cartDataService).saveCart(any(Cart.class));

        CartCookieResponseDTO result = assertDoesNotThrow(() -> cartWriteUseCase.addProductForCart(addCartDTOList, null, cartMemberDTO.uid()));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result.responseMessage());
        assertNull(result.cookieValue());
    }

    @Test
    @DisplayName(value = "장바구니에 상품 추가. 장바구니가 존재하지 않는데 사용자 정보 조회가 안되는 경우")
    void addProductForCartWrongUser() {
        CartMemberDTO cartMemberDTO = getCartMemberDTO();
        AddCartDTO addCartDTO1 = new AddCartDTO(1L, 2);
        AddCartDTO addCartDTO2 = new AddCartDTO(2L, 2);
        List<AddCartDTO> addCartDTOList = List.of(addCartDTO1, addCartDTO2);

        when(cartDomainService.setCartMemberDTO(any(), any())).thenReturn(cartMemberDTO);
        when(cartDataService.getCartData(any(CartMemberDTO.class))).thenReturn(null);
        when(userDataService.getMemberByUserIdOrElseNull(any())).thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> cartWriteUseCase.addProductForCart(addCartDTOList, null, cartMemberDTO.uid())
        );
    }

    @Test
    @DisplayName(value = "장바구니 상품 수량 증가")
    void cartCountUp() {
        CartMemberDTO cartMemberDTO = getCartMemberDTO();
        Cart userCart = Cart.builder().id(1L).build();
        CartDetail countUpDetail = CartDetail.builder().id(1L).cart(userCart).build();

        when(cartDomainService.setCartMemberDTO(any(), any())).thenReturn(cartMemberDTO);
        when(cartDataService.getCartData(any(CartMemberDTO.class))).thenReturn(userCart);
        when(cartDataService.getCartDetail(anyLong())).thenReturn(countUpDetail);
        doNothing().when(cartDataService).saveCartDetail(any(CartDetail.class));

        CartCookieResponseDTO result = assertDoesNotThrow(() -> cartWriteUseCase.cartCountUp(1L, null, cartMemberDTO.uid()));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result.responseMessage());
        assertNull(result.cookieValue());
    }

    @Test
    @DisplayName(value = "장바구니 상품 수량 증가. 장바구니 데이터가 없는 경우")
    void cartCountUpCartIsNull() {
        CartMemberDTO cartMemberDTO = getCartMemberDTO();

        when(cartDomainService.setCartMemberDTO(any(), any())).thenReturn(cartMemberDTO);
        when(cartDataService.getCartData(any(CartMemberDTO.class))).thenReturn(null);

        assertThrows(
                CustomNotFoundException.class,
                () -> cartWriteUseCase.cartCountUp(1L, null, cartMemberDTO.uid())
        );
    }

    @Test
    @DisplayName(value = "장바구니 상품 수량 증가. CartDetail의 cartId와 Cart의 id가 일치하지 않는 경우")
    void cartCountUpNotEqualsCartAndDetail() {
        CartMemberDTO cartMemberDTO = getCartMemberDTO();
        Cart userCart = Cart.builder().id(1L).build();
        CartDetail countUpDetail = CartDetail.builder()
                .id(1L)
                .cart(Cart.builder().id(2L).build())
                .build();

        when(cartDomainService.setCartMemberDTO(any(), any())).thenReturn(cartMemberDTO);
        when(cartDataService.getCartData(any(CartMemberDTO.class))).thenReturn(userCart);
        when(cartDataService.getCartDetail(anyLong())).thenReturn(countUpDetail);

        assertThrows(
                IllegalArgumentException.class,
                () -> cartWriteUseCase.cartCountUp(1L, null, cartMemberDTO.uid())
        );
    }

    @Test
    @DisplayName(value = "장바구니 상품 수량 감소")
    void cartCountDown() {
        CartMemberDTO cartMemberDTO = getCartMemberDTO();
        Cart userCart = Cart.builder().id(1L).build();
        CartDetail countUpDetail = CartDetail.builder().id(1L).cart(userCart).build();

        when(cartDomainService.setCartMemberDTO(any(), any())).thenReturn(cartMemberDTO);
        when(cartDataService.getCartData(any(CartMemberDTO.class))).thenReturn(userCart);
        when(cartDataService.getCartDetail(anyLong())).thenReturn(countUpDetail);
        doNothing().when(cartDataService).saveCartDetail(any(CartDetail.class));

        CartCookieResponseDTO result = assertDoesNotThrow(() -> cartWriteUseCase.cartCountDown(1L, null, cartMemberDTO.uid()));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result.responseMessage());
        assertNull(result.cookieValue());
    }

    @Test
    @DisplayName(value = "장바구니 상품 수량 감소. 장바구니 데이터가 없는 경우")
    void cartCountDownCartIsNull() {
        CartMemberDTO cartMemberDTO = getCartMemberDTO();

        when(cartDomainService.setCartMemberDTO(any(), any())).thenReturn(cartMemberDTO);
        when(cartDataService.getCartData(any(CartMemberDTO.class))).thenReturn(null);

        assertThrows(
                CustomNotFoundException.class,
                () -> cartWriteUseCase.cartCountDown(1L, null, cartMemberDTO.uid())
        );
    }

    @Test
    @DisplayName(value = "장바구니 상품 수량 감소. CartDetail의 cartId와 Cart의 id가 일치하지 않는 경우")
    void cartCountDownNotEqualsCartAndDetail() {
        CartMemberDTO cartMemberDTO = getCartMemberDTO();
        Cart userCart = Cart.builder().id(1L).build();
        CartDetail countUpDetail = CartDetail.builder()
                .id(1L)
                .cart(Cart.builder().id(2L).build())
                .build();

        when(cartDomainService.setCartMemberDTO(any(), any())).thenReturn(cartMemberDTO);
        when(cartDataService.getCartData(any(CartMemberDTO.class))).thenReturn(userCart);
        when(cartDataService.getCartDetail(anyLong())).thenReturn(countUpDetail);

        assertThrows(
                IllegalArgumentException.class,
                () -> cartWriteUseCase.cartCountDown(1L, null, cartMemberDTO.uid())
        );
    }

    @Test
    @DisplayName(value = "장바구니에서 선택 상품 삭제. 일부 삭제인 경우")
    void deleteSelectProductFromCart() {
        CartMemberDTO cartMemberDTO = getCartMemberDTO();
        List<Long> deleteSelectIds = new ArrayList<>(List.of(1L));
        Long cartId = 1L;
        List<Long> allUserCartDetailIds = new ArrayList<>(List.of(1L, 2L));

        when(cartDomainService.setCartMemberDTO(any(), any())).thenReturn(cartMemberDTO);
        when(cartDataService.getUserCartId(any(CartMemberDTO.class))).thenReturn(cartId);
        when(cartDataService.getAllUserCartDetailId(anyLong())).thenReturn(allUserCartDetailIds);
        doNothing().when(cartDomainService).validateDeleteIdsFromUserDetailIds(anyList(), anyList());
        doNothing().when(cartDataService).deleteSelectProductFromCartDetail(anyList());

        CartCookieResponseDTO result = assertDoesNotThrow(() -> cartWriteUseCase.deleteSelectProductFromCart(deleteSelectIds, null, cartMemberDTO.uid()));

        verify(cartDataService, never()).deleteUserCart(anyLong());
        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result.responseMessage());
        assertNull(result.cookieValue());
    }

    @Test
    @DisplayName(value = "장바구니에서 선택 상품 삭제. 전체 삭제인 경우")
    void deleteSelectProductFromCartAllSelect() {
        CartMemberDTO cartMemberDTO = getCartMemberDTO();
        List<Long> deleteSelectIds = new ArrayList<>(List.of(1L, 2L));
        Long cartId = 1L;
        List<Long> allUserCartDetailIds = new ArrayList<>(List.of(1L, 2L));

        when(cartDomainService.setCartMemberDTO(any(), any())).thenReturn(cartMemberDTO);
        when(cartDataService.getUserCartId(any(CartMemberDTO.class))).thenReturn(cartId);
        when(cartDataService.getAllUserCartDetailId(anyLong())).thenReturn(allUserCartDetailIds);
        doNothing().when(cartDomainService).validateDeleteIdsFromUserDetailIds(anyList(), anyList());
        doNothing().when(cartDataService).deleteUserCart(anyLong());

        CartCookieResponseDTO result = assertDoesNotThrow(() -> cartWriteUseCase.deleteSelectProductFromCart(deleteSelectIds, null, cartMemberDTO.uid()));

        verify(cartDataService, never()).deleteSelectProductFromCartDetail(anyList());
        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result.responseMessage());
        assertNull(result.cookieValue());
    }

    @Test
    @DisplayName(value = "장바구니에서 선택 상품 삭제. 장바구니 데이터가 없는 경우")
    void deleteSelectProductFromCartIsNull() {
        CartMemberDTO cartMemberDTO = getCartMemberDTO();
        List<Long> deleteSelectIds = new ArrayList<>(List.of(1L, 2L));

        when(cartDomainService.setCartMemberDTO(any(), any())).thenReturn(cartMemberDTO);
        when(cartDataService.getUserCartId(any(CartMemberDTO.class))).thenReturn(null);

        assertThrows(
                CustomNotFoundException.class,
                () -> cartWriteUseCase.deleteSelectProductFromCart(deleteSelectIds, null, cartMemberDTO.uid())
        );
    }

    @Test
    @DisplayName(value = "장바구니 전체 삭제")
    void deleteAllProductFromCart() {
        CartMemberDTO cartMemberDTO = getCartMemberDTO();
        Long cartId = 1L;

        when(cartDomainService.setCartMemberDTO(any(), any())).thenReturn(cartMemberDTO);
        when(cartDataService.getUserCartId(any(CartMemberDTO.class))).thenReturn(cartId);
        doNothing().when(cartDataService).deleteUserCart(anyLong());

        String result = assertDoesNotThrow(() -> cartWriteUseCase.deleteAllProductFromCart(null, cartMemberDTO.uid()));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result);
    }

    @Test
    @DisplayName(value = "장바구니 전체 삭제. 장바구니 데이터가 없는 경우")
    void deleteAllProductFromCartIsNull() {
        CartMemberDTO cartMemberDTO = getCartMemberDTO();

        when(cartDomainService.setCartMemberDTO(any(), any())).thenReturn(cartMemberDTO);
        when(cartDataService.getUserCartId(any(CartMemberDTO.class))).thenReturn(null);

        assertThrows(
                CustomNotFoundException.class,
                () -> cartWriteUseCase.deleteAllProductFromCart(null, cartMemberDTO.uid())
        );
    }
}
