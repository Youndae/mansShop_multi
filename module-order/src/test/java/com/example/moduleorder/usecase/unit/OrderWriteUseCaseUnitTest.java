package com.example.moduleorder.usecase.unit;

import com.example.modulecart.model.dto.business.CartMemberDTO;
import com.example.modulecart.service.CartDomainService;
import com.example.modulecart.service.CartReader;
import com.example.modulecommon.customException.CustomAccessDeniedException;
import com.example.modulecommon.customException.CustomNotFoundException;
import com.example.modulecommon.customException.CustomOrderSessionExpiredException;
import com.example.modulecommon.model.entity.Cart;
import com.example.modulecommon.model.entity.CartDetail;
import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.ProductOption;
import com.example.modulecommon.model.enumuration.Result;
import com.example.moduleorder.fixture.OrderUnitFixture;
import com.example.moduleorder.model.dto.business.OrderDataDTO;
import com.example.moduleorder.model.dto.in.OrderProductRequestDTO;
import com.example.moduleorder.model.dto.out.OrderDataResponseDTO;
import com.example.moduleorder.model.vo.OrderItemVO;
import com.example.moduleorder.model.vo.PreOrderDataVO;
import com.example.moduleorder.service.OrderCookieWriter;
import com.example.moduleorder.service.OrderDataService;
import com.example.moduleorder.service.OrderDomainService;
import com.example.moduleorder.usecase.OrderWriteUseCase;
import com.example.moduleproduct.model.dto.product.business.OrderProductInfoDTO;
import com.example.moduleproduct.service.product.ProductOptionReader;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class OrderWriteUseCaseUnitTest {

    @InjectMocks
    private OrderWriteUseCase orderWriteUseCase;

    @Mock
    private CartDomainService cartDomainService;

    @Mock
    private OrderDataService orderDataService;

    @Mock
    private OrderDomainService orderDomainService;

    @Mock
    private ProductOptionReader productOptionReader;

    @Mock
    private OrderCookieWriter orderCookieWriter;

    @Mock
    private CartReader cartReader;

    @Test
    @DisplayName(value = "상품 페이지에서 선택한 상품 바로 구매를 위한 상품 데이터 조회")
    void orderDataProcessAfterPayment() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        OrderProductRequestDTO dto1 = new OrderProductRequestDTO(1L, 3);
        OrderProductRequestDTO dto2 = new OrderProductRequestDTO(2L, 2);
        List<OrderProductRequestDTO> optionIdAndCountDTO = List.of(dto1, dto2);
        List<Long> optionIds = List.of(1L, 2L);
        List<OrderProductInfoDTO> orderDataDTO = OrderUnitFixture.createOrderProductInfoDTOList();
        OrderDataResponseDTO responseDTO = OrderUnitFixture.createOrderDataResponseDTO();

        when(productOptionReader.findOrderData(anyList())).thenReturn(orderDataDTO);
        when(orderDomainService.mappingOrderResponseDTO(anyList(), anyList())).thenReturn(responseDTO);
        when(orderCookieWriter.createAndSetOrderTokenCookie(response)).thenReturn("testResponseToken");
        doNothing().when(orderDataService).setOrderValidateDataToRedis(any(), any(PreOrderDataVO.class));

        OrderDataResponseDTO result = assertDoesNotThrow(() -> orderWriteUseCase.getProductOrderData(optionIdAndCountDTO, null, response, "tester"));

        assertNotNull(result);
        assertEquals(responseDTO, result);
    }

    @Test
    @DisplayName(value = "상품 페이지에서 선택한 상품 바로 구매를 위한 상품 데이터 조회. 데이터가 없는 경우")
    void getProductOrderDataEmpty() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        OrderProductRequestDTO dto1 = new OrderProductRequestDTO(1L, 3);
        OrderProductRequestDTO dto2 = new OrderProductRequestDTO(2L, 2);
        List<OrderProductRequestDTO> optionIdAndCountDTO = List.of(dto1, dto2);

        when(productOptionReader.findOrderData(anyList())).thenReturn(Collections.emptyList());
        assertThrows(
                IllegalArgumentException.class,
                () -> orderWriteUseCase.getProductOrderData(optionIdAndCountDTO, null, response, "tester")
        );
    }

    @Test
    @DisplayName(value = "장바구니 선택 상품 주문을 위한 상품 데이터 조회.")
    void getCartOrderData() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        List<Long> cartDetailIds = List.of(1L, 2L);
        Cart cart = Cart.builder()
                .id(1L)
                .member(Member.builder().userId("testUser").build())
                .cookieId(null)
                .build();
        CartDetail cartDetail1 = CartDetail.builder()
                .id(1L)
                .cart(cart)
                .productOption(ProductOption.builder().id(1L).build())
                .cartCount(3)
                .build();
        CartDetail cartDetail2 = CartDetail.builder()
                .id(2L)
                .cart(cart)
                .productOption(ProductOption.builder().id(2L).build())
                .cartCount(2)
                .build();
        List<CartDetail> cartDetails = List.of(cartDetail1, cartDetail2);
        CartMemberDTO cartMemberDTO = new CartMemberDTO(cart.getMember().getUserId(), null);
        List<OrderProductInfoDTO> orderDataDTO = OrderUnitFixture.createOrderProductInfoDTOList();
        OrderDataResponseDTO responseDTO = OrderUnitFixture.createOrderDataResponseDTO();

        when(cartDomainService.setCartMemberDTO(any(), any())).thenReturn(cartMemberDTO);
        when(cartReader.findAllCartDetailByIds(anyList())).thenReturn(cartDetails);
        when(cartReader.findCartById(anyLong())).thenReturn(cart);
        when(productOptionReader.findOrderData(anyList())).thenReturn(orderDataDTO);
        when(orderDomainService.mappingOrderResponseDTO(anyList(), anyList())).thenReturn(responseDTO);
        when(orderCookieWriter.createAndSetOrderTokenCookie(response)).thenReturn("testResponseToken");
        doNothing().when(orderDataService).setOrderValidateDataToRedis(any(), any(PreOrderDataVO.class));

        OrderDataResponseDTO result = assertDoesNotThrow(() -> orderWriteUseCase.getCartOrderData(cartDetailIds, null, null, cartMemberDTO.uid(), response));

        assertNotNull(result);
        assertEquals(responseDTO, result);
    }

    @Test
    @DisplayName(value = "장바구니 선택 상품 주문을 위한 상품 데이터 조회. 장바구니 상세 데이터가 없는 경우")
    void getCartOrderDataDetailEmpty() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        List<Long> cartDetailIds = List.of(1L, 2L);
        CartMemberDTO cartMemberDTO = new CartMemberDTO("testUser", null);

        when(cartDomainService.setCartMemberDTO(any(), any())).thenReturn(cartMemberDTO);
        when(cartReader.findAllCartDetailByIds(anyList())).thenReturn(Collections.emptyList());

        assertThrows(
                CustomNotFoundException.class,
                () -> orderWriteUseCase.getCartOrderData(cartDetailIds, null, null, cartMemberDTO.uid(), response)
        );
    }

    @Test
    @DisplayName(value = "장바구니 선택 상품 주문을 위한 상품 데이터 조회. 조회한 장바구니가 사용자 데이터가 아닌 경우")
    void getCartOrderDataDetailAccessDenied() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        List<Long> cartDetailIds = List.of(1L, 2L);
        CartMemberDTO cartMemberDTO = new CartMemberDTO("testUser", null);
        Cart cart = Cart.builder()
                .id(1L)
                .member(Member.builder().userId("wrongUser").build())
                .cookieId(null)
                .build();
        CartDetail cartDetail1 = CartDetail.builder()
                .id(1L)
                .cart(cart)
                .productOption(ProductOption.builder().id(1L).build())
                .cartCount(3)
                .build();
        CartDetail cartDetail2 = CartDetail.builder()
                .id(2L)
                .cart(cart)
                .productOption(ProductOption.builder().id(2L).build())
                .cartCount(2)
                .build();
        List<CartDetail> cartDetails = List.of(cartDetail1, cartDetail2);

        when(cartDomainService.setCartMemberDTO(any(), any())).thenReturn(cartMemberDTO);
        when(cartReader.findAllCartDetailByIds(anyList())).thenReturn(cartDetails);
        when(cartReader.findCartById(anyLong())).thenReturn(cart);

        assertThrows(
                CustomAccessDeniedException.class,
                () -> orderWriteUseCase.getCartOrderData(cartDetailIds, null, null, cartMemberDTO.uid(), response)
        );
    }

    @Test
    @DisplayName(value = "결제 API 호출 전 데이터 검증")
    void validateOrder() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        OrderDataResponseDTO validateDTO = OrderUnitFixture.createOrderDataResponseDTO();
        List<OrderItemVO> orderData = new ArrayList<>();
        for(OrderDataDTO data : validateDTO.orderData()) {
            orderData.add(
                    new OrderItemVO(
                            data.productId(),
                            data.optionId(),
                            data.count(),
                            data.price()
                    )
            );
        }
        PreOrderDataVO cachingData = new PreOrderDataVO("testUser", orderData, validateDTO.totalPrice());
        String orderTokenValue = "orderTokenValue";
        when(orderDomainService.extractOrderTokenValue(any())).thenReturn("orderTokenValue");
        when(orderDataService.getCachingOrderData(any())).thenReturn(cachingData);
        when(orderDomainService.validateOrderData(any(PreOrderDataVO.class), any(OrderDataResponseDTO.class), any()))
                .thenReturn(true);

        String result = assertDoesNotThrow(() -> orderWriteUseCase.validateOrderData(validateDTO, "testUser", new Cookie("order", orderTokenValue), response));

        assertNotNull(result);
        assertEquals(Result.OK.getResultKey(), result);
    }

    @Test
    @DisplayName(value = "결제 API 호출 전 데이터 검증. orderToken이 없는 경우")
    void validateOrderTokenIsNull() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        OrderDataResponseDTO validateDTO = OrderUnitFixture.createOrderDataResponseDTO();

        when(orderDomainService.extractOrderTokenValue(any())).thenThrow(CustomOrderSessionExpiredException.class);

        assertThrows(
                CustomOrderSessionExpiredException.class,
                () -> orderWriteUseCase.validateOrderData(validateDTO, "testUser", null, response)
        );
    }

    @Test
    @DisplayName(value = "결제 API 호출 전 데이터 검증. 검증 데이터가 존재하지 않는 경우")
    void validateOrderValidateDataIsNull() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        OrderDataResponseDTO validateDTO = OrderUnitFixture.createOrderDataResponseDTO();
        String orderTokenValue = "orderTokenValue";

        when(orderDomainService.extractOrderTokenValue(any())).thenReturn(orderTokenValue);
        when(orderDataService.getCachingOrderData(any())).thenReturn(null);

        assertThrows(
                CustomOrderSessionExpiredException.class,
                () -> orderWriteUseCase.validateOrderData(validateDTO, "testUser", new Cookie("order", orderTokenValue), response)
        );
    }

    @Test
    @DisplayName(value = "결제 API 호출 전 데이터 검증. 검증 데이터와 일치하지 않는 경우")
    void validateOrderInValidData() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        OrderDataResponseDTO validateDTO = OrderUnitFixture.createOrderDataResponseDTO();
        List<OrderItemVO> orderData = new ArrayList<>();
        for(OrderDataDTO data : validateDTO.orderData()) {
            orderData.add(
                    new OrderItemVO(
                            data.productId(),
                            data.optionId() + 1,
                            data.count(),
                            data.price()
                    )
            );
        }
        PreOrderDataVO cachingData = new PreOrderDataVO("testUser", orderData, validateDTO.totalPrice());
        String orderTokenValue = "orderTokenValue";
        when(orderDomainService.extractOrderTokenValue(any())).thenReturn("orderTokenValue");
        when(orderDataService.getCachingOrderData(any())).thenReturn(cachingData);
        when(orderDomainService.validateOrderData(any(PreOrderDataVO.class), any(OrderDataResponseDTO.class), any()))
                .thenReturn(false);
        doNothing().when(orderCookieWriter).deleteOrderTokenCookie(response);
        doNothing().when(orderDataService).deleteOrderTokenData(any());

        assertThrows(
                CustomOrderSessionExpiredException.class,
                () -> orderWriteUseCase.validateOrderData(validateDTO, "testUser", new Cookie("order", orderTokenValue), response)
        );
    }
}
