package com.example.moduleorder.model.dto.in;

import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.ProductOrder;
import com.example.modulecommon.model.enumuration.OrderStatus;
import com.example.modulecommon.utils.PhoneNumberUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.List;

@Schema(name = "주문 요청 데이터")
public record PaymentDTO(
        @Schema(name = "recipient", description = "수령인")
        @NotBlank(message = "수령인은 필수 데이터입니다.")
        String recipient,

        @Schema(name = "phone", description = "수령인 연락처")
        @NotBlank(message = "연락처는 필수 데이터입니다.")
        @Pattern(
                regexp = "^[0-9]{10,11}$",
                message = "전화번호는 - 를 제외한 숫자 10 ~ 11자리여야 합니다."
        )
        String phone,

        @Schema(name = "orderMemo", description = "주문 메모")
        String orderMemo,

        @Schema(name = "address", description = "배송지")
        @NotBlank(message = "배송지는 필수 데이터입니다.")
        String address,

        // 주문 상품 정보는 캐싱된 주문 데이터와 비교 검증에서 처리
        @Schema(name = "orderProduct", description = "주문 상품 정보", type = "array")
        List<OrderProductDTO> orderProduct,

        @Schema(name = "deliveryFee", description = "배송비")
        @Min(value = 0, message = "Integrity delivery fee")
        int deliveryFee,

        @Schema(name = "totalPrice", description = "결제 금액")
        @Min(value = 0, message = "Integrity total price")
        int totalPrice,

        @Schema(name = "paymentType", description = "결제 타입(card, cash)")
        @NotBlank(message = "결제 타입은 필수 데이터입니다.")
        String paymentType,

        @Schema(name = "orderType", description = "주문 요청 방식(cart, direct)")
        @NotBlank(message = "잘못된 값입니다.")
        String orderType
) {

    public ProductOrder toOrderEntity(String uid, LocalDateTime createdAt) {
        return ProductOrder.builder()
                .member(
                        Member.builder()
                                .userId(uid)
                                .build()
                )
                .recipient(recipient)
                .orderPhone(PhoneNumberUtils.format(phone))
                .orderAddress(address)
                .orderMemo(orderMemo)
                .orderTotalPrice(totalPrice)
                .deliveryFee(deliveryFee)
                .paymentType(paymentType)
                .orderStat(OrderStatus.ORDER.getStatusStr())
                .createdAt(createdAt)
                .build();
    }
}
