package com.example.moduleorder.service;

import com.example.modulecart.model.dto.business.CartMemberDTO;
import com.example.modulecommon.model.entity.*;
import com.example.modulecommon.model.enumuration.OrderStatus;
import com.example.modulecommon.utils.PhoneNumberUtils;
import com.example.moduleorder.model.dto.business.OrderDataDTO;
import com.example.moduleorder.model.dto.business.OrderListDetailDTO;
import com.example.moduleorder.model.dto.business.ProductOrderDataDTO;
import com.example.moduleorder.model.dto.in.OrderProductDTO;
import com.example.moduleorder.model.dto.in.OrderProductRequestDTO;
import com.example.moduleorder.model.dto.in.PaymentDTO;
import com.example.moduleorder.model.dto.out.OrderDataResponseDTO;
import com.example.moduleorder.model.dto.out.OrderListDTO;
import com.example.moduleproduct.model.dto.product.business.OrderProductInfoDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class OrderDomainServiceUnitTest {

    @InjectMocks
    private OrderDomainService orderDomainService;

    private List<OrderProductDTO> getOrderProductDTOList() {
        OrderProductDTO orderProductDTO1 = new OrderProductDTO(
                1L,
                "testProductName",
                "testProductId",
                3,
                30000
        );
        OrderProductDTO orderProductDTO2 = new OrderProductDTO(
                2L,
                "testProductName",
                "testProductId",
                2,
                20000
        );

        return new ArrayList<>(List.of(orderProductDTO1, orderProductDTO2));
    }

    @Test
    @DisplayName(value = "주문 데이터 매핑")
    void mapProductOrderDataDTO() {
        CartMemberDTO cartMemberDTO = new CartMemberDTO("tester", null);
        List<OrderProductDTO> orderProductDTOList = getOrderProductDTOList();
        PaymentDTO paymentDTO = new PaymentDTO(
                "testRecipient",
                "01012345678",
                "testMemo",
                "testRecipient Address",
                orderProductDTOList,
                3500,
                53500,
                "card",
                "cart"
        );
        ProductOrder orderFixture = ProductOrder.builder()
                .member(Member.builder().userId(cartMemberDTO.uid()).build())
                .recipient(paymentDTO.recipient())
                .orderPhone(paymentDTO.phone())
                .orderAddress(paymentDTO.address())
                .orderMemo(paymentDTO.orderMemo())
                .orderTotalPrice(paymentDTO.totalPrice())
                .deliveryFee(paymentDTO.deliveryFee())
                .paymentType(paymentDTO.paymentType())
                .orderStat(OrderStatus.ORDER.getStatusStr())
                .createdAt(LocalDateTime.now())
                .productCount(5)
                .build();
        List<String> orderProductIds = List.of(orderProductDTOList.get(0).getProductId());
        List<Long> orderOptionIds = List.of(1L, 2L);

        ProductOrderDataDTO result = assertDoesNotThrow(() -> orderDomainService.mapProductOrderDataDTO(paymentDTO, cartMemberDTO, orderFixture.getCreatedAt()));

        assertNotNull(result);

        ProductOrder resultOrder = result.productOrder();
        assertEquals(orderFixture.getMember().getUserId(), resultOrder.getMember().getUserId());
        assertEquals(orderFixture.getRecipient(), resultOrder.getRecipient());
        assertEquals(PhoneNumberUtils.format(orderFixture.getOrderPhone()), resultOrder.getOrderPhone());
        assertEquals(orderFixture.getOrderAddress(), resultOrder.getOrderAddress());
        assertEquals(orderFixture.getOrderMemo(), resultOrder.getOrderMemo());
        assertEquals(orderFixture.getOrderTotalPrice(), resultOrder.getOrderTotalPrice());
        assertEquals(orderFixture.getDeliveryFee(), resultOrder.getDeliveryFee());
        assertEquals(orderFixture.getPaymentType(), resultOrder.getPaymentType());
        assertEquals(orderFixture.getOrderStat(), resultOrder.getOrderStat());
        assertEquals(orderFixture.getCreatedAt(), resultOrder.getCreatedAt());
        assertEquals(orderFixture.getProductCount(), resultOrder.getProductCount());

        assertEquals(paymentDTO.orderProduct(), result.orderProductList());
        assertEquals(orderProductIds, result.orderProductIds());
        assertEquals(orderOptionIds, result.orderOptionIds());
    }

    @Test
    @DisplayName(value = "검증을 위한 OrderProductDTO와 ProductOption 조회 결과 매핑")
    void mapValidateFieldList() {
        List<OrderProductDTO> orderProductDTOList = getOrderProductDTOList();
        List<ProductOption> productOptionList = new ArrayList<>();
        for(int i = 0; i < orderProductDTOList.size(); i++) {
            OrderProductDTO orderProductDTO = orderProductDTOList.get(i);
            productOptionList.add(
                    ProductOption.builder()
                            .id(orderProductDTO.getOptionId())
                            .size("size" + i)
                            .color("color" + i)
                            .build()
            );
        }

        List<OrderDataDTO> orderDataDTOList = new ArrayList<>();
        for(int i = 0; i < orderProductDTOList.size(); i++) {
            OrderProductDTO orderProductDTO = orderProductDTOList.get(i);
            ProductOption productOption = productOptionList.get(i);
            orderDataDTOList.add(
                    new OrderDataDTO(
                            orderProductDTO.getProductId(),
                            orderProductDTO.getOptionId(),
                            orderProductDTO.getProductName(),
                            productOption.getSize(),
                            productOption.getColor(),
                            orderProductDTO.getDetailCount(),
                            orderProductDTO.getDetailPrice()
                    )
            );
        }

        List<OrderDataDTO> result = assertDoesNotThrow(() -> orderDomainService.mapValidateFieldList(orderProductDTOList, productOptionList));

        assertNotNull(result);
        assertEquals(orderDataDTOList.size(), result.size());
        assertEquals(orderDataDTOList, result);
    }

    @Test
    @DisplayName(value = "사용자에게 전달할 주문 데이터 매핑")
    void mappingOrderResponseDTO() {
        OrderProductRequestDTO orderProductRequestDTO1 = new OrderProductRequestDTO(1L, 3);
        OrderProductRequestDTO orderProductRequestDTO2 = new OrderProductRequestDTO(2L, 2);
        List<OrderProductRequestDTO> orderProductRequestDTOList = new ArrayList<>(List.of(orderProductRequestDTO1, orderProductRequestDTO2));
        OrderProductInfoDTO orderProductInfoDTO1 = new OrderProductInfoDTO(
                "testProductId",
                1L,
                "testProductName",
                "size1",
                "color1",
                10000
        );
        OrderProductInfoDTO orderProductInfoDTO2 = new OrderProductInfoDTO(
                "testProductId",
                2L,
                "testProductName",
                "size2",
                "color2",
                10000
        );
        List<OrderProductInfoDTO> orderProductInfoDTOList = new ArrayList<>(List.of(orderProductInfoDTO1, orderProductInfoDTO2));
        List<OrderDataDTO> orderDataDTOList = new ArrayList<>();
        int totalPrice = 0;
        for(int i = 0; i < orderProductRequestDTOList.size(); i++) {
            OrderProductRequestDTO orderProductRequestDTO = orderProductRequestDTOList.get(i);
            OrderProductInfoDTO orderProductInfoDTO = orderProductInfoDTOList.get(i);
            orderDataDTOList.add(
                    new OrderDataDTO(
                            orderProductInfoDTO.productId(),
                            orderProductInfoDTO.optionId(),
                            orderProductInfoDTO.productName(),
                            orderProductInfoDTO.size(),
                            orderProductInfoDTO.color(),
                            orderProductRequestDTO.count(),
                            orderProductInfoDTO.price() * orderProductRequestDTO.count()
                    )
            );
            totalPrice += orderProductInfoDTO.price() * orderProductRequestDTO.count();
        }

        OrderDataResponseDTO result = assertDoesNotThrow(() -> orderDomainService.mappingOrderResponseDTO(orderProductRequestDTOList, orderProductInfoDTOList));

        assertNotNull(result);
        assertEquals(orderDataDTOList.size(), result.orderData().size());
        assertEquals(orderDataDTOList, result.orderData());
        assertEquals(totalPrice, result.totalPrice());
    }

    @Test
    @DisplayName(value = "주문 목록 데이터 매핑")
    void mapOrderListDTO() {
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
        List<ProductOrder> productOrderList = new ArrayList<>(List.of(productOrder1, productOrder2));
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
        List<OrderListDTO> resultFixture = List.of(fixtureDTO1, fixtureDTO2);

        List<OrderListDTO> result = orderDomainService.mapOrderListDTO(productOrderList, orderListDetailDTOList);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(resultFixture, result);
    }
}
