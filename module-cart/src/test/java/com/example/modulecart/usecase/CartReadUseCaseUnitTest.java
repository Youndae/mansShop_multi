package com.example.modulecart.usecase;

import com.example.modulecart.model.dto.business.CartMemberDTO;
import com.example.modulecart.model.dto.out.CartDetailDTO;
import com.example.modulecart.service.CartDataService;
import com.example.modulecart.service.CartDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartReadUseCaseUnitTest {

    @InjectMocks
    private CartReadUseCase cartReadUseCase;

    @Mock
    private CartDataService cartDataService;

    @Mock
    private CartDomainService cartDomainService;

    @Test
    @DisplayName(value = "장바구니 목록 조회")
    void getCartList() {
        CartMemberDTO cartMemberDTO = new CartMemberDTO("tester", null);
        Long userCartId = 1L;
        CartDetailDTO cartDetailDTO1 = new CartDetailDTO(
                1L,
                "testProductId",
                1L,
                "testProductName",
                "testProductThumbnail",
                "size1",
                "color1",
                5,
                10000,
                45000,
                10
        );
        CartDetailDTO cartDetailDTO2 = new CartDetailDTO(
                2L,
                "testProductId",
                2L,
                "testProductName",
                "testProductThumbnail",
                "size2",
                "color2",
                1,
                10000,
                9000,
                10
        );
        List<CartDetailDTO> cartDetailDTOList = List.of(cartDetailDTO1, cartDetailDTO2);

        when(cartDomainService.setCartMemberDTO(any(), any())).thenReturn(cartMemberDTO);
        when(cartDataService.getUserCartId(any(CartMemberDTO.class))).thenReturn(userCartId);
        when(cartDataService.getCartList(anyLong())).thenReturn(cartDetailDTOList);

        List<CartDetailDTO> result = assertDoesNotThrow(() -> cartReadUseCase.getCartList(null, cartMemberDTO.uid()));

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(cartDetailDTOList, result);
    }

    @Test
    @DisplayName(value = "장바구니 목록 조회. 장바구니 데이터가 없는 경우")
    void getCartListCartIsNull() {
        CartMemberDTO cartMemberDTO = new CartMemberDTO("tester", null);

        when(cartDomainService.setCartMemberDTO(any(), any())).thenReturn(cartMemberDTO);
        when(cartDataService.getUserCartId(any(CartMemberDTO.class))).thenReturn(null);

        List<CartDetailDTO> result = assertDoesNotThrow(() -> cartReadUseCase.getCartList(null, cartMemberDTO.uid()));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
