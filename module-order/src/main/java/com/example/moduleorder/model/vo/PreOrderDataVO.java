package com.example.moduleorder.model.vo;

import java.util.List;

public record PreOrderDataVO(
        String userId,
        List<OrderItemVO> orderData,
        int totalPrice
) {
}
