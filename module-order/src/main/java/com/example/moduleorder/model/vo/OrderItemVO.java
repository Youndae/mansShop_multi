package com.example.moduleorder.model.vo;

public record OrderItemVO(
        String productId,
        Long optionId,
        int count,
        int price
) {

}

