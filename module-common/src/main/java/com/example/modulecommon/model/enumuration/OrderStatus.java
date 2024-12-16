package com.example.modulecommon.model.enumuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    ORDER("주문 확인중"),
    PREPARATION("상품 준비중"),
    SHIPPING("배송중"),
    COMPLETE("배송 완료");

    private final String statusStr;
}
