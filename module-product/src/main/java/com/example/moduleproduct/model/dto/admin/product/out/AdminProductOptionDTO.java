package com.example.moduleproduct.model.dto.admin.product.out;

public record AdminProductOptionDTO(
        Long optionId,
        String size,
        String color,
        int optionStock,
        boolean optionIsOpen
) {
}
