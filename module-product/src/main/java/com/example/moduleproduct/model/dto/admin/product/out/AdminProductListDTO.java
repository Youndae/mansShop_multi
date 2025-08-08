package com.example.moduleproduct.model.dto.admin.product.out;

public record AdminProductListDTO(
        String productId,
        String classification,
        String productName,
        int stock,
        Long optionCount,
        int price
) {
}