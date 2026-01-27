package com.example.moduleorder.model.enumuration;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum OrderRequestType {
    // 장바구니로부터 주문
    CART("cart"),
    // 상품 상세페이지에서 바로 주문
    DIRECT("direct");

    private final String type;

    public static void validate(String orderType) {
        boolean exists = Arrays.stream(values())
                .anyMatch(v -> v.type.equalsIgnoreCase(orderType));

        if(!exists)
            throw new IllegalArgumentException("Invalid OrderRequestType : " + orderType);
    }
}
