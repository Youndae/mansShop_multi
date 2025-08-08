package com.example.moduleproduct.model.dto.admin.product.out;

import com.example.modulecommon.model.entity.Product;
import com.example.modulecommon.utils.ProductDiscountUtils;

public record AdminDiscountResponseDTO(
        String productId,
        String classification,
        String productName,
        int price,
        int discount,
        int totalPrice
) {

    public AdminDiscountResponseDTO(Product product) {
        this(
                product.getId(),
                product.getClassification().getId(),
                product.getProductName(),
                product.getProductPrice(),
                product.getProductDiscount(),
                ProductDiscountUtils.calcDiscountPrice(product.getProductPrice(), product.getProductDiscount())
        );
    }
}
