package com.example.moduleorder.fixture;

import com.example.modulecommon.model.entity.*;
import com.example.moduleorder.model.dto.business.OrderDataDTO;
import com.example.moduleorder.model.dto.out.OrderDataResponseDTO;
import com.example.moduleproduct.model.dto.product.business.OrderProductInfoDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderUnitFixture {

    // 결제 API 호출 전 검증 시의 RequestDTO
    // 사용자는 비회원처리
    public static OrderDataResponseDTO createOrderDataResponseDTO() {
        OrderDataDTO orderDataDTO1 = new OrderDataDTO(
                "testProduct1",
                1L,
                "testProductName1",
                "testSize",
                "testColor",
                3,
                30000
        );

        OrderDataDTO orderDataDTO2 = new OrderDataDTO(
                "testProduct2",
                2L,
                "testProductName2",
                "testSize2",
                "testColor2",
                2,
                20000
        );
        List<OrderDataDTO> orderDataDTOs = List.of(orderDataDTO1, orderDataDTO2);

        return new OrderDataResponseDTO(orderDataDTOs, 50000);
    }

    // 상품 페이지에서 바로구매 요청 시의 RequestDTO
    public static List<OrderProductInfoDTO> createOrderProductInfoDTOList() {
        OrderProductInfoDTO optionDTO1 = new OrderProductInfoDTO(
                "testProductId1",
                1L,
                "testProductName1",
                "testSize1",
                "testColor1",
                30000
        );

        OrderProductInfoDTO optionDTO2 = new OrderProductInfoDTO(
                "testProductId1",
                2L,
                "testProductName1",
                "testSize2",
                "testColor2",
                20000
        );

        return List.of(optionDTO1, optionDTO2);
    }

    // Cart 엔티티와 연관 엔티티인 CartDetail List 생성
    public static Cart createCart() {
        Cart cartEntity = Cart.builder()
                .id(1L)
                .member(
                        Member.builder()
                                .userId("testUser")
                                .build()
                )
                .cookieId(null)
                .build();
        CartDetail cartDetail1 = CartDetail.builder()
                .cart(cartEntity)
                .productOption(ProductOption.builder().id(1L).build())
                .cartCount(3)
                .build();

        CartDetail cartDetail2 = CartDetail.builder()
                .cart(cartEntity)
                .productOption(ProductOption.builder().id(2L).build())
                .cartCount(4)
                .build();

        List<CartDetail> cartDetails = List.of(cartDetail1, cartDetail2);

        cartDetails.forEach(cartEntity::addCartDetail);

        return cartEntity;
    }

    // 장바구니에서 선택 상품 주문 요청 처리 중 DB 조회 결과 리스트
    public static List<OrderProductInfoDTO> createOrderCartDTOList() {
        OrderProductInfoDTO optionDTO1 = new OrderProductInfoDTO(
                "testProductId1",
                1L,
                "testProductName1",
                "testSize1",
                "testColor1",
                10000
        );

        OrderProductInfoDTO optionDTO2 = new OrderProductInfoDTO(
                "testProductId1",
                2L,
                "testProductName1",
                "testSize1",
                "testColor1",
                20000
        );

        return List.of(optionDTO1, optionDTO2);
    }

    public static List<ProductOrder> createProductOrderList(List<Member> memberList, Product product) {
        List<ProductOrder> productOrders = new ArrayList<>();
        for(int i = 0; i < memberList.size(); i++){
            Member member = memberList.get(i);
            ProductOrder productOrder = createProductOrder(i, member);
            createProductOrderDetailList(productOrder, product);
            productOrders.add(productOrder);
        }

        return productOrders;
    }

    public static ProductOrder createProductOrder(int id, Member member) {
        return ProductOrder.builder()
                .id((long) id)
                .member(member)
                .recipient(member.getUserName())
                .orderPhone(member.getPhone())
                .orderAddress(member.getUserName() + "'s home")
                .orderMemo(member.getUserName() + "'s memo")
                .orderTotalPrice(10000)
                .deliveryFee(3500)
                .createdAt(LocalDateTime.now())
                .productCount(2)
                .build();
    }

    public static List<ProductOrderDetail> createProductOrderDetailList(ProductOrder productOrder, Product product) {
        List<ProductOrderDetail> productOrderDetails = new ArrayList<>();

        for(int i = 0; i < 2; i++) {
            ProductOrderDetail detail = ProductOrderDetail.builder()
                    .productOption(product.getProductOptions().get(i))
                    .product(product)
                    .productOrder(productOrder)
                    .orderDetailCount(1)
                    .orderDetailPrice(5000)
                    .orderReviewStatus(false)
                    .build();

            productOrder.addDetail(detail);
            productOrderDetails.add(detail);
        }

        return productOrderDetails;
    }
}
