package com.example.modulecart.model.dto.out;

import com.example.modulecommon.utils.ProductDiscountUtils;

public record CartDetailDTO(
        long cartDetailId,
        String productId,
        long optionId,
        String productName,
        String productThumbnail,
        String size,
        String color,
        int count,
        int originPrice,
        int price,
        int discount
) {
    public CartDetailDTO(long cartDetailId,
                         String productId,
                         long optionId,
                         String productName,
                         String productThumbnail,
                         String size,
                         String color,
                         int count,
                         int price,
                         int discount) {
        this(
                cartDetailId,
                productId,
                optionId,
                productName,
                productThumbnail,
                size,
                color,
                count,
                price * count,
                ProductDiscountUtils.calcDiscountPrice(price, discount) * count,
                discount
        );
    }
}
