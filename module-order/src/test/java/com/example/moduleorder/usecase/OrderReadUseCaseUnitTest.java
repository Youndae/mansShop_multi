package com.example.moduleorder.usecase;

import com.example.modulecommon.model.dto.response.PagingListDTO;
import com.example.modulecommon.model.dto.response.PagingMappingDTO;
import com.example.modulecommon.model.entity.ProductOrder;
import com.example.modulecommon.model.enumuration.OrderStatus;
import com.example.moduleorder.model.dto.business.OrderListDetailDTO;
import com.example.moduleorder.model.dto.in.MemberOrderDTO;
import com.example.moduleorder.model.dto.out.OrderListDTO;
import com.example.moduleorder.model.dto.page.OrderPageDTO;
import com.example.moduleorder.service.OrderDataService;
import com.example.moduleorder.service.OrderDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderReadUseCaseUnitTest {

    @InjectMocks
    private OrderReadUseCase orderReadUseCase;

    @Mock
    private OrderDataService orderDataService;

    @Mock
    private OrderDomainService orderDomainService;

    @Test
    @DisplayName(value = "주문 목록 조회")
    void getOrderList() {
        OrderPageDTO pageDTO = new OrderPageDTO(1, "3");
        MemberOrderDTO memberOrderDTO = new MemberOrderDTO("testUser", null, null);
        ProductOrder productOrder1 = ProductOrder.builder()
                .id(1L)
                .orderTotalPrice(30000)
                .createdAt(LocalDateTime.now())
                .orderStat(OrderStatus.PREPARATION.getStatusStr())
                .build();
        ProductOrder productOrder2 = ProductOrder.builder()
                .id(2L)
                .orderTotalPrice(10000)
                .createdAt(LocalDateTime.now())
                .orderStat(OrderStatus.ORDER.getStatusStr())
                .build();
        List<ProductOrder> productOrderList = new ArrayList<>(List.of(productOrder1, productOrder2));
        Page<ProductOrder> order = new PageImpl<>(productOrderList);
        OrderListDetailDTO orderListDetailDTO1 = new OrderListDetailDTO(
                productOrder1.getId(),
                "testProduct1",
                1L,
                1L,
                "testProductName1",
                "testSize1",
                "testColor1",
                1,
                15000,
                false,
                "testProductThumb1"
        );
        OrderListDetailDTO orderListDetailDTO2 = new OrderListDetailDTO(
                productOrder1.getId(),
                "testProduct2",
                2L,
                2L,
                "testProductName2",
                "testSize2",
                "testColor2",
                1,
                15000,
                false,
                "testProductThumb2"
        );
        OrderListDetailDTO orderListDetailDTO3 = new OrderListDetailDTO(
                productOrder2.getId(),
                "testProduct3",
                3L,
                3L,
                "testProductName3",
                "testSize3",
                "testColor3",
                1,
                10000,
                false,
                "testProductThumb1"
        );
        List<OrderListDetailDTO> orderListDetailDTOList = new ArrayList<>(List.of(orderListDetailDTO1, orderListDetailDTO2, orderListDetailDTO3));

        OrderListDTO fixtureDTO1 = new OrderListDTO(
                productOrder1.getId(),
                productOrder1.getOrderTotalPrice(),
                productOrder1.getCreatedAt(),
                productOrder1.getOrderStat(),
                List.of(orderListDetailDTO1, orderListDetailDTO2)
        );
        OrderListDTO fixtureDTO2 = new OrderListDTO(
                productOrder2.getId(),
                productOrder2.getOrderTotalPrice(),
                productOrder2.getCreatedAt(),
                productOrder2.getOrderStat(),
                List.of(orderListDetailDTO3)
        );
        List<OrderListDTO> contentList = List.of(fixtureDTO1, fixtureDTO2);
        PagingMappingDTO pagingMappingDTO = new PagingMappingDTO(order.getTotalElements(), order.isEmpty(), order.getNumber(), order.getTotalPages());
        PagingListDTO<OrderListDTO> resultFixture = new PagingListDTO<>(contentList, pagingMappingDTO);

        when(orderDataService.findAllByUserId(any(MemberOrderDTO.class), any(OrderPageDTO.class))).thenReturn(order);
        when(orderDataService.getAllOrderDetailByOrderIds(anyList())).thenReturn(orderListDetailDTOList);
        when(orderDomainService.mapOrderListDTO(anyList(), anyList())).thenReturn(contentList);

        PagingListDTO<OrderListDTO> result = assertDoesNotThrow(() -> orderReadUseCase.getOrderList(pageDTO, memberOrderDTO));

        assertNotNull(result);
        assertFalse(result.content().isEmpty());
        assertEquals(resultFixture.content(), result.content());
        assertEquals(resultFixture.pagingData().getTotalPages(), resultFixture.pagingData().getTotalPages());
        assertEquals(resultFixture.pagingData().getTotalElements(), resultFixture.pagingData().getTotalElements());
        assertEquals(resultFixture.pagingData().getNumber(), resultFixture.pagingData().getNumber());
        assertEquals(resultFixture.pagingData().isEmpty(), resultFixture.pagingData().isEmpty());
    }
}
